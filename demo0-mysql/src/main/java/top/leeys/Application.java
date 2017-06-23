package top.leeys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import top.leeys.controller.NativeServlet;

import javax.servlet.http.HttpServlet;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {
    /**
     * 1.修改AppConfig.java里数据库的用户名与密码
     * 2.运行main方法
     * 3.浏览器访问: http://localhost:8080/demo0
     */
    public static void main(String args[]) {
        SpringApplication.run(Application.class, args);
    }


    @Override
    public SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    @Bean
    public HttpServlet nativeServlet() {
        return new NativeServlet();
    }

    @Bean
    public ServletRegistrationBean dispatcherServletRegistration() {
        ServletRegistrationBean registration = new ServletRegistrationBean();
        registration.setLoadOnStartup(1);
        registration.setServlet(nativeServlet());
        registration.addUrlMappings("/native_servlet");
        return registration;
    }
    
}