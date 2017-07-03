package top.leeys.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import lombok.extern.slf4j.Slf4j;
import top.leeys.annotation.Admin;
import top.leeys.config.ErrorCode;
import top.leeys.exception.BaseException;
import top.leeys.repository.AdminRepository;


/**
 * 管理员拦截器
 * 
 * @author leeys.top@gmail.com
 */
@Slf4j
public class AdminInterceptor extends HandlerInterceptorAdapter {
    
    @Autowired AdminRepository adminRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (handler.getClass().isAssignableFrom(HandlerMethod.class)) {
            HandlerMethod handlerMethod = (HandlerMethod)handler;
            Admin admin = handlerMethod.getMethodAnnotation(Admin.class);
            if (admin != null) {  //管理员权限
                //TODO 普通管理员权限检验
                
                if (admin.value()) { 
                    //超级管理员权限检验
                    String token = request.getHeader("token");
                    log.debug("token: {}", token);
                    if (StringUtils.isEmpty(token)) {
                        throw new BaseException(ErrorCode.LIMITED_AUTHORITY, "权限不足");
                    }
                    top.leeys.domain.Admin adminUser = adminRepository.findByToken(token);
                    if (adminUser == null) {
                        throw new BaseException(ErrorCode.LIMITED_AUTHORITY, "权限不足");
                    }
                }
            }
        }
        return true; //拦截器继续执行
    }
    
}
	
