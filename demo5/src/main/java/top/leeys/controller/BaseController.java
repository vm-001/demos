package top.leeys.controller;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import top.leeys.lucene.BookLucene;
import top.leeys.repository.AdminRepository;
import top.leeys.repository.BookRepository;
import top.leeys.repository.OrderItemRepository;
import top.leeys.repository.OrderRepository;
import top.leeys.repository.UserRepository;
import top.leeys.service.AlipayService;
import top.leeys.service.EmailService;

/**
 * 集中收集组件
 */
@Component
public class BaseController {
    @Autowired BookLucene bookLucene;
    
    @Autowired BookRepository bookRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired UserRepository userRepository;
    @Autowired OrderItemRepository orderItemRepository;
    @Autowired AdminRepository adminRepository;
    
    @Autowired AlipayService alipayService;
    @Autowired EmailService emailService;
    
    @Autowired DataSource dataSource;
    @Autowired JdbcTemplate jdbcTemplate;
    
    @Autowired RestTemplate rest;
}
