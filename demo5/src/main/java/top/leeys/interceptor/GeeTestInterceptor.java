package top.leeys.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import lombok.extern.slf4j.Slf4j;
import top.leeys.annotation.GeeTest;
import top.leeys.config.AppConfig;
import top.leeys.config.ErrorCode;
import top.leeys.exception.BaseException;
import top.leeys.util.GeetestLib;


/**
 * 极验拦截器
 * 
 * @author leeys.top@gmail.com
 */
@Slf4j
public class GeeTestInterceptor extends HandlerInterceptorAdapter {

    private static final GeetestLib gtSdk = new GeetestLib(AppConfig.GeeTest.GEETEST_ID, AppConfig.GeeTest.GEETEST_KEY, true);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (handler.getClass().isAssignableFrom(HandlerMethod.class)) {
            HandlerMethod handlerMethod = (HandlerMethod)handler;
            if (handlerMethod.getMethodAnnotation(GeeTest.class) != null) {
                String challenge = request.getParameter("challenge");
                String validate = request.getParameter("validate");
                String seccode = request.getParameter("seccode");
                log.debug("in geetest interceptor, challenge: {}, validate: {}, seccode: {}", challenge, validate, seccode);
                try {
                    boolean result = geeTest(challenge, validate, seccode, request);
                    if (!result) {
                        throw new BaseException(ErrorCode.GEETEST_VALIDATE_ERROR, "geetest error");
                    }
                } catch (Exception e) {
                    throw new BaseException(ErrorCode.UNKNOWN_ERROR, "session expired");  //
                }
                System.out.println("校验成功");
            }
        }
        return true; //拦截器继续执行
    }
    
    
    public boolean geeTest(String challenge, String validate, String seccode, 
            HttpServletRequest request) {
        //当session过期清除时这里会抛出NullPointerException
        int gt_server_status_code = (Integer) request.getSession().getAttribute(gtSdk.gtServerStatusSessionKey);
        // 从session中获取userid
        String userid = (String) request.getSession().getAttribute("userid");
        int gtResult = 0;
        if (gt_server_status_code == 1) {
            // gt-server正常，向gt-server进行二次验证
            gtResult = gtSdk.enhencedValidateRequest(challenge, validate, seccode, userid);
            System.out.println(gtResult);
        } else {
            // gt-server非正常情况下，进行failback模式验证
            System.out.println("failback:use your own server captcha validate");
            gtResult = gtSdk.failbackValidateRequest(challenge, validate, seccode);
            System.out.println(gtResult);
        }
        System.out.println("gtResult:" + gtResult);
        return gtResult == 1;
    }
}
	
