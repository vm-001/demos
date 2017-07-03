package top.leeys.service;

import static top.leeys.config.AppConfig.AliPay.ALIPAY_PUBLIC_KEY;
import static top.leeys.config.AppConfig.AliPay.APP_ID;
import static top.leeys.config.AppConfig.AliPay.APP_PRIVATE_KEY;
import static top.leeys.config.AppConfig.AliPay.CHARSET;
import static top.leeys.config.AppConfig.AliPay.GATEWAY;
import static top.leeys.config.AppConfig.AliPay.PAY_NOTIFY_URL;
import static top.leeys.config.AppConfig.AliPay.RETURN_URL;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AlipayService {
    @Autowired ObjectMapper mapper;
    
    //可共用
    private static final AlipayClient ALIPAY_CLIENT = 
            new DefaultAlipayClient(GATEWAY, APP_ID, APP_PRIVATE_KEY, "json", "UTF-8", ALIPAY_PUBLIC_KEY);
    
  
    
    public String buildForm(Map<String, String> params) {
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();// 创建API对应的request
        alipayRequest.setReturnUrl(RETURN_URL);
        alipayRequest.setNotifyUrl(PAY_NOTIFY_URL);
        //待请求参数数组       seller_id
//        params.put("seller_id", SELLER_ID);
        String form = "";
        try {
            alipayRequest.setBizContent(mapper.writeValueAsString(params));
            form = ALIPAY_CLIENT.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            log.error("支付宝生成表单失败", e);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        log.debug("支付宝生成表单：\n" + form);
        return form;
    }
    
    public boolean verifyNotify(Map<String, String> params) throws AlipayApiException {
        return AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, CHARSET);
    }
    
}
