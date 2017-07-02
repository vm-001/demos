package top.leeys.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 限制客户端请求次数
 * 
 * @author leeys.top@gmail.com
 */
@Documented
@Inherited
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    /**
     * 每天最多请求次数
     */
    int value() default 0;
}
