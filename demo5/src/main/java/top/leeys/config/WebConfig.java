package top.leeys.config;

import javax.sql.DataSource;

import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;

import top.leeys.interceptor.AdminInterceptor;
import top.leeys.interceptor.GeeTestInterceptor;

@Configuration
@EnableWebMvc       // MVC
@EnableAsync        // 异步任务
//@EnableScheduling   // 计划任务
@EnableTransactionManagement // 启用注解式事务管理 <tx:annotation-driven />
public class WebConfig extends WebMvcConfigurerAdapter {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/*").addResourceLocations("classpath:/public/"); // 资源匹配

//        registry.addResourceHandler("/js/*").addResourceLocations("classpath:/public/");
//        registry.addResourceHandler("/css/*").addResourceLocations("classpath:/public/");
//        registry.addResourceHandler("/images/*").addResourceLocations("classpath:/public/");
        
    }
    
    
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//            .allowedOrigins("*");
//    }

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
        properties.setInitialSize(1);
        properties.setMaxActive(3);
        properties.setMinIdle(1);
        properties.setMaxWait(1000 * 20);
        properties.setTestOnBorrow(false);
        properties.setTestOnReturn(false);
        properties.setFairQueue(false);
        properties.setTestWhileIdle(true);
        properties.setValidationQuery("SELECT 1");
        properties.setTimeBetweenEvictionRunsMillis(1000 * 60 * 5);
        properties.setMinEvictableIdleTimeMillis(1000 * 60 * 30); // 被标记为空闲连接最长还能存活1小时
        return new org.apache.tomcat.jdbc.pool.DataSource(properties);
    }
    
    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
    
//    @Bean
//    public TaskExecutor taskExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(5);
//        executor.setMaxPoolSize(10);
//        executor.setQueueCapacity(25);
//        return executor;
//    }
    
//    @Bean
//    public MappedInterceptor myMappedInterceptor() {
//        return new MappedInterceptor(new String[]{"/users/**"}, userInterceptor());
//        return new MappedInterceptor(new String[]{"/**"}, userInterceptor());
//        
//    }
    

    @Bean
    public GeeTestInterceptor geeTestInterceptor() {
        return new GeeTestInterceptor();
    }
    
    @Bean
    public AdminInterceptor adminInterceptor() {
       return new AdminInterceptor(); 
    }
    

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(geeTestInterceptor());
        registry.addInterceptor(adminInterceptor());
    }
}