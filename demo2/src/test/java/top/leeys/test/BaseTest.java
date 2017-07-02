package top.leeys.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import top.leeys.Application;
import top.leeys.repository.RecordRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration  //web项目
public class BaseTest {
    
    @Autowired RecordRepository recordRepository;
    
    @Test
    public void test() {
        System.out.println(recordRepository.getTodayPlayNum("298e08e9b34048e88414a14bddf654e9"));
    }
}
