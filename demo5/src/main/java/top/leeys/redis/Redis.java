package top.leeys.redis;


import static top.leeys.config.AppConfig.Redis.ip;
import static top.leeys.config.AppConfig.Redis.password;
import static top.leeys.config.AppConfig.Redis.port;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;
/**
 * redis操作类，自带连接池
 * 
 * @author leeys.top@gmail.com
 */
public class Redis {
	
	private static JedisPoolConfig jedisPoolConfig;
	private static JedisPool jedisPool;
	
	static {
		 /**
		  * jedis连接的timeout为 2000秒（33分钟）, 具体是指？
		  * 原理是：
		  * 清除线程每30秒运行一次，标记出池中空闲时间超过10分钟的连接，此时并不会立即清除连接，
		  * 当清除线程再次标记时，发现连接已经被标记过，并且距上次标记的时间超过60秒，则会清除该空闲连接，每次清除3条连接（默认）
		  * by leeys.top@gmail.com
		  */
		 jedisPoolConfig = new JedisPoolConfig();
		 jedisPoolConfig.setMaxTotal(8);  //默认是8  最大连接数
		 jedisPoolConfig.setMaxIdle(8);   //默认是8  最大空闲连接数
		 jedisPoolConfig.setMinIdle(3);   //池中最小空闲连接数
		 jedisPoolConfig.setFairness(true);  //公平队列
		 jedisPoolConfig.setTestWhileIdle(true);  //开启清除空闲连接
		 jedisPoolConfig.setMaxWaitMillis(1000 * 60 * 30); //池中连接最大空闲时间为30分钟
		 jedisPoolConfig.setTimeBetweenEvictionRunsMillis(1000 * 60); //清除线程每60秒运行一次
		 jedisPoolConfig.setMinEvictableIdleTimeMillis(1000 * 60 * 10); //被标记为空闲状态的连接最长还能再池中存活10分钟

		 jedisPool = new JedisPool(jedisPoolConfig, ip, port, 0, password);
	}
	

	
	private static void close(Jedis jedis) {
	    if (jedis != null) {
	        try {
	            jedis.close();
	        } catch (JedisException e) {
	            e.printStackTrace();
	        }
	    }
	}
	
	
	public static interface RedisCallback {
	    void callback(Jedis jedis);
	}
	/**
	 * 执行不带返回类型的回调
	 * @param redisCallback
	 */
    public static void execute(RedisCallback redisCallback) {
        Jedis jedis = jedisPool.getResource();
        redisCallback.callback(jedis);
        close(jedis);
    }
	
    /**
     * 带返回参数的回调
     * @param <T> 返回类型
     */
    public static interface Callback<T> {
        T callback(Jedis jedis);
    }
    /**
     * 执行带返回参数的回调
     */
    public static <T> T executeByType(Callback<T> callback) {
        Jedis jedis = jedisPool.getResource();
        T t =  callback.callback(jedis);
        close(jedis);
        return t;
    }
    
	public static String get(String key) {
	    Jedis jedis = jedisPool.getResource();
	    String v = jedis.get(key);
	    jedis.close();
	    return v;
	}
}
