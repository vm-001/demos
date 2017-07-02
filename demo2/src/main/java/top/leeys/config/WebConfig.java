package top.leeys.config;


import javax.sql.DataSource;

import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;

import top.leeys.interceptor.RateLimitedInterceptor;
import top.leeys.interceptor.TokenInterceptor;


@Configuration
@EnableWebMvc
@EnableTransactionManagement //same as <tx:annotation-driven />
public class WebConfig extends WebMvcConfigurerAdapter {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/*").addResourceLocations("classpath:/public/");
    }
    
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseSuffixPatternMatch(false); //cancel SuffixMatch
    }

    @Bean
    public DataSource dataSource() {
        PoolProperties properties = new PoolProperties();
        properties.setDriverClassName("com.mysql.jdbc.Driver");
        properties.setUrl(AppConfig.JDBC.url);
        properties.setUrl(AppConfig.JDBC.url
                + "&useServerPrepStmts=true&cachePrepStmts=true&prepStmtCacheSize=250&prepStmtCacheSqlLimit=2048&cacheCallableStmts=true");
        properties.setUsername(AppConfig.JDBC.username);
        properties.setPassword(AppConfig.JDBC.password);
        properties.setInitialSize(10);
        properties.setMaxActive(10);
        properties.setMinIdle(10);
        properties.setMaxWait(1000 * 20);
        properties.setFairQueue(false);
        properties.setTestWhileIdle(true);
        properties.setValidationQuery("SELECT 1");
        properties.setTimeBetweenEvictionRunsMillis(1000 * 60 * 5);  //驱逐线程运行频率
        properties.setMinEvictableIdleTimeMillis(1000 * 60 * 30);    //空闲连接最大存活时间
        return new org.apache.tomcat.jdbc.pool.DataSource(properties);
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource());
        return jdbcTemplate;
    }
    
    @Bean
    public RateLimitedInterceptor timelimitedInterceptor() {
        return new RateLimitedInterceptor();
    }
    
    @Bean
    public TokenInterceptor tokenInterceptor() {
        return new TokenInterceptor();
    }
    
    
    /*增加拦截器 */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor());
        registry.addInterceptor(timelimitedInterceptor());
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public ObjectMapper mapper() {
        return new ObjectMapper();
    }
}