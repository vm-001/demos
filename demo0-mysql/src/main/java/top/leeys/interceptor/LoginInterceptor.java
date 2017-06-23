package top.leeys.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import top.leeys.annotation.Login;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 登录状态拦截器
 * @author leeys.top@gmail.com
 *
 */
@Slf4j
public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        
        HandlerMethod handlerMethod = (HandlerMethod)handler;
        if (handler.getClass().isAssignableFrom(HandlerMethod.class)) {
            //当客户端访问的控制器有@Login注解
            if (handlerMethod.getMethodAnnotation(Login.class) != null) {
                String sid = (String) request.getSession().getAttribute("username");
                log.info("in LoginInterceptor! session attribute sid : " + sid);
                if (sid == null) {
                    response.sendRedirect("login.html");
                    return false;
                }
            }
        }
        return true; //拦截器继续执行
    }
	
}
