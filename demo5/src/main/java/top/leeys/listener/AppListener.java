package top.leeys.listener;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import top.leeys.redis.Redis;
import top.leeys.repository.BookRepository;

/**
 * 应用启动时初始化一些数据
 * 
 * @author leeys.top@gmail.com
 */
@Component
@Slf4j
public class AppListener {
    @Autowired BookRepository bookRepository;
    
    /**
     * 35W行记录count时需要100ms, 该方法用来缓存数据库行记录数
     */
    @PostConstruct
    public void initRedis() {
        long num = bookRepository.count();
        Redis.execute( jedis -> jedis.set("demo5.book.num", num + ""));
        log.debug("缓存数据记录行数:" + num);
    }
}
