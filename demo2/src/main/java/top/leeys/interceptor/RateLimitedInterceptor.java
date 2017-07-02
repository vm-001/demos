package top.leeys.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import lombok.extern.slf4j.Slf4j;
import top.leeys.annotation.RateLimited;
import top.leeys.config.ErrorCode;
import top.leeys.domain.User;
import top.leeys.exception.BaseException;
import top.leeys.repository.RecordRepository;


/**
 * 频率限制拦截器
 * 
 * 如果必要的话还可以根据该用户两次提交分数的分值差和时间差来判断是否作弊
 * 伪代码：
 *     deltaScore 
 *     deltaSeconds
 *     if (some condition)
 *       redis.setTTL("deny_" + uuid, "true", 1天)
 *       
 * @author leeys.top@gmail.com
 */
@Slf4j
public class RateLimitedInterceptor extends HandlerInterceptorAdapter{
    
    @Autowired RecordRepository recordRepository;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (handler.getClass().isAssignableFrom(HandlerMethod.class)) {
            HandlerMethod handlerMethod = (HandlerMethod)handler;
            RateLimited rateLimited = handlerMethod.getMethodAnnotation(RateLimited.class);
            if (rateLimited != null) {
                int rateValue = rateLimited.value();
                String uuid = ((User) request.getAttribute("user")).getUuid();
                int today = recordRepository.getTodayPlayNum(uuid);
                if (today > rateValue) {
                    throw new BaseException(ErrorCode.RATE_LIMITED, "rate limited");
                }
            }
        }
        return true;
    }
}
