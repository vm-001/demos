package top.leeys.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@EnableWebMvc
@EnableTransactionManagement // 启用注解式事务管理 <tx:annotation-driven />
@ComponentScan(basePackages = "top.leeys")
public class WebConfig extends WebMvcConfigurerAdapter {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/*").addResourceLocations("classpath:/public/"); // 资源匹配

    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configuer) {
        configuer.setUseSuffixPatternMatch(false); // 取消url前缀匹配
    }
    

    /* c3p0 */
//    @Bean
//    public DataSource dataSource() {
//        ComboPooledDataSource dataSource = new ComboPooledDataSource();
//        try {
//            dataSource.setDriverClass("com.mysql.jdbc.Driver");
//            dataSource.setJdbcUrl(AppConfig.JDBC.url + "&useServerPrepStmts=true&cachePrepStmts=true&prepStmtCacheSize=250&prepStmtCacheSqlLimit=2048");
//            dataSource.setUser(AppConfig.JDBC.username);
//            dataSource.setPassword(AppConfig.JDBC.password);
//
//            dataSource.setInitialPoolSize(30);
//            dataSource.setMaxPoolSize(30);
//            dataSource.setMinPoolSize(30);
//            dataSource.setMaxIdleTime(60 * 30);
//        } catch (PropertyVetoException e) {
//            e.printStackTrace();
//        }
//        return dataSource;
//    }

    /* Hikari */
    @Bean
    public HikariDataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setJdbcUrl(AppConfig.JDBC.url + "&useServerPrepStmts=true&cachePrepStmts=true&prepStmtCacheSize=250&prepStmtCacheSqlLimit=2048&cacheCallableStmts=true");
//        config.setJdbcUrl(AppConfig.JDBC.url);
        config.setUsername(AppConfig.JDBC.username);
        config.setPassword(AppConfig.JDBC.password);
        config.setMaximumPoolSize(30);
        config.setConnectionTimeout(20000);
        HikariDataSource ds = new HikariDataSource(config);
        return ds;
    }

    /* TomcatJDBC */
//    @Bean
//    public DataSource dataSource() {
//        PoolProperties properties = new PoolProperties();
//        properties.setDriverClassName("com.mysql.jdbc.Driver");
//        properties.setUrl(AppConfig.JDBC.url);
//        properties.setUrl(AppConfig.JDBC.url + "&useServerPrepStmts=true&cachePrepStmts=true&prepStmtCacheSize=250&prepStmtCacheSqlLimit=2048&cacheCallableStmts=true");
//        properties.setUsername(AppConfig.JDBC.username);
//        properties.setPassword(AppConfig.JDBC.password);
//        properties.setInitialSize(30);
//        properties.setMaxActive(30);
//        properties.setMinIdle(30);
//        properties.setMaxWait(1000 * 20);
//        properties.setTestOnBorrow(false);
//        properties.setTestOnReturn(false);
//        properties.setFairQueue(false);
//        properties.setTestWhileIdle(true);
//        properties.setValidationQuery("SELECT 1");
//        properties.setTimeBetweenEvictionRunsMillis(1000 * 60 * 5);
//        properties.setMinEvictableIdleTimeMillis(1000 * 60 * 30);    //被标记为空闲连接最长还能存活1小时
//        return new org.apache.tomcat.jdbc.pool.DataSource(properties);
//    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource());
        return jdbcTemplate;
    }

    /* 增加拦截器 */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new LoginInterceptor());  //自定义拦截器, 并发测试期间关闭
    }
}