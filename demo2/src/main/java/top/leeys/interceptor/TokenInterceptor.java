package top.leeys.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import top.leeys.annotation.Token;
import top.leeys.config.ErrorCode;
import top.leeys.domain.User;
import top.leeys.exception.BaseException;
import top.leeys.service.UserService;


/**
 * 拦截request从header中获取token的值
 * 
 * @author leeys.top@gmail.com
 */
public class TokenInterceptor extends HandlerInterceptorAdapter{
    @Autowired UserService userService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        
        if (handler.getClass().isAssignableFrom(HandlerMethod.class)) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;//强转
            if (handlerMethod.getMethodAnnotation(Token.class) != null) {
                String token = request.getHeader("token");
                if (token == null || token.trim().length() == 0) {
                    throw new BaseException(ErrorCode.LIMITED_AUTHORITY, "你长得真帅"); //missing param(token) in header
                }
                User user = userService.findByToken(token);
                if (user == null) {
                    throw new BaseException(ErrorCode.TOKEN_ERROR, "你长得真帅");  // token error
                }
                request.setAttribute("user", user);
            }
        }
        return true;
    }
}
