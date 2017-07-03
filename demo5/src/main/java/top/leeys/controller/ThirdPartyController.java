package top.leeys.controller;

import static top.leeys.config.AppConfig.AliPay.APP_ID;
import static top.leeys.config.AppConfig.AliPay.SELLER_ID;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alipay.api.AlipayApiException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import top.leeys.config.AppConfig;
import top.leeys.domain.Order;
import top.leeys.util.GeetestLib;

@RestController
@RequestMapping(value = "/", name = "第三方API")
@Slf4j
public class ThirdPartyController extends BaseController{

    /**
     * 支付宝的回调接口
     */
    @PostMapping("/callback/alipay/pay_notify")
    public void verify(
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.debug("支付宝调用接口");
        Enumeration<String> names = request.getParameterNames();
        Map<String, String> params = new HashMap<String, String>();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            params.put(name, request.getParameter(name));
        }
        log.debug("支付宝回调参数：" + params);
        
        //订单id
        String orderId = params.getOrDefault("out_trade_no", "");
        //订单总支付价格
        float price = Float.valueOf(params.getOrDefault("total_amount", "0"));
        //appid
        String appId = params.getOrDefault("app_id", "");
        //卖家支付宝账号
        String sellerId = params.getOrDefault("seller_id", "");
        
        boolean result = false;
        try {
            if (alipayService.verifyNotify(params)) {   //确认是支付宝官方的回调
                log.debug("支付宝回调验证成功");
                Order order = orderRepository.findOne(orderId);
                System.out.println(new ObjectMapper().writeValueAsString(order));
                //根据支付宝要求进行二次校验
                if (order != null && 
                    price == order.getPrice() &&    //校验支付价格是否等于订单总价格
                    appId.equals(APP_ID) &&         //校验appid是否为商户本身
                    sellerId.equals(SELLER_ID)      //校验收款人是否为商户本身
                    ) {
                    log.debug("支付二次校验通过");
                    result = true;
                    order.setStatus(Order.Status.SUCCESS);
                    orderRepository.save(order);
                }
                
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        
        String out = result ? "success" : "failure";
        response.getWriter().print(out);
        response.getWriter().flush();
        response.getWriter().close();
    }
    
    
    /**
     * 极验接口
     * @return {"success":1,"gt":"aa446ffxxxxx","challenge":"7c55ab204e0caexxxxx"}
     */
    @GetMapping(value = "/geetest")
    public void geeTestGet(
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        GeetestLib gtSdk = new GeetestLib(AppConfig.GeeTest.GEETEST_ID, AppConfig.GeeTest.GEETEST_KEY, true);
        String resStr = "{}";
        //自定义userid
        String userid = "test";
        //进行验证预处理
        int gtServerStatus = gtSdk.preProcess(userid);
        //将服务器状态设置到session中
        request.getSession().setAttribute(gtSdk.gtServerStatusSessionKey, gtServerStatus);
        //将userid设置到session中
        request.getSession().setAttribute("userid", userid);
        resStr = gtSdk.getResponseStr();
        PrintWriter out = response.getWriter();
        out.print(resStr);
        out.flush();
        out.close();
    }
}
