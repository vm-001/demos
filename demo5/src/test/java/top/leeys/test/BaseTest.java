package top.leeys.test;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import top.leeys.Application;
import top.leeys.domain.Book;
import top.leeys.lucene.BookLucene;
import top.leeys.repository.AdminRepository;
import top.leeys.repository.BookRepository;
import top.leeys.repository.OrderItemRepository;
import top.leeys.repository.OrderRepository;
import top.leeys.repository.UserRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
//@WebAppConfiguration  //web项目
@Slf4j
public class BaseTest {
    @Autowired BookLucene bookLucene;
    @Autowired BookRepository bookDao;
    @Autowired UserRepository userDao;
    @Autowired OrderRepository orderDao;
    @Autowired OrderItemRepository orderItemDao;
    @Autowired AdminRepository adminDao;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired RestTemplate rest;
    
    
    @Test
    public void test() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        Proxy proxy= new Proxy(Type.HTTP, new InetSocketAddress("122.112.253.67", 80));
        requestFactory.setProxy(proxy);
        rest =  new RestTemplate(requestFactory);
        
        String sql = "select * from tb_book where img_url = '' order by id desc";
        jdbcTemplate.query(sql, rs -> {
            Integer id = rs.getInt("id");
            String isbn = rs.getString("isbn");
            if (!StringUtils.isEmpty(isbn)) {
                
                try {
                    String data  = rest.getForObject("https://api.douban.com/v2/book/isbn/" + isbn, String.class);
                    JSONObject json;
                    json = new JSONObject(data);
//                    String imgL = json.getJSONObject("images").getString("large");
                    String imgM = json.getJSONObject("images").getString("medium");
                    Book book = bookDao.findOne(id);
                    book.setImgUrl(imgM);
                    bookDao.save(book);
                    System.out.println("更新成功：" + id + " " + imgM);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    log.error("失败：" + e.getMessage());
                }
            }
        });
    }
}
