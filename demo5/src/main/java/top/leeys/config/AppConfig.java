package top.leeys.config;

/**
 * 应用全局配置
 * 
 * (隐藏了大部分个人配置信息) 
 *
 * @author leeys.top@gmail.com
 */
public interface AppConfig {
    
    /*兼容window和linux*/
    String CLASSPATH = 
            AppConfig.class.getClassLoader().getResource("").getPath().contains(":") ?
            AppConfig.class.getClassLoader().getResource("").getPath().substring(1) : 
            AppConfig.class.getClassLoader().getResource("").getPath();
    
    /*开发者模式*/
    boolean DEVELOPER = true;
    
//    String HOST = "http://localhost:8080";
    String HOST = "http://demo.leeys.top";
    
    String CONTEXT_NAME = "/demo5";
    
    
    interface Redis {
        int port = 6379;
        String ip = "YOUR_IP";
        String password = "YOUR_PASSWORD";
    }
    
	/**
	 * 邮箱配置
	 */
	interface Email {
	    String HOST = "smtp.qq.com";
	    String TITLE = "[天天书屋]";
	    String FROM = "YOUR_EMAIL";
	    String TOKEN = "YOUR_EMAIL_TOKEN";
	}
	
	/**
	 * 极验配置
	 */
	interface GeeTest {
		String GEETEST_ID = "YOUR_ID";
		String GEETEST_KEY = "YOUR_KEY";
	}
	/**
	 * 数据库配置
	 */
    interface JDBC {
        String driverClass = "com.mysql.jdbc.Driver";
        String database = "demo5";
        String username = "";
        String password = "";
        String url = "jdbc:mysql://127.0.0.1:3306/"+ database +"?characterEncoding=UTF-8&createDatabaseIfNotExist=true";
    }
    
	
    /**
     * 支付宝配置
     */
	interface AliPay {
	    //应用号 
	    String APP_ID = "YOUR_APP_ID";
	    //商户的私钥
	    String APP_PRIVATE_KEY = "YOUR_APP_PRIVATE_KEY";
	    String CHARSET = "UTF-8";
	    //支付宝公钥
	    String ALIPAY_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDIgHnOn7LLILlKETd6BFRJ0GqgS2Y3mn1wMQmyh9zEyWlz5p1zrahRahbXAfCfSqshSNfqOmAQzSHRVjCqjsAw1jyqrXaPdKBmr90DIpIxmIyKXv4GGAkPyJ/6FTFY99uhpiq0qadD/uSzQsefWo0aTvP/65zi3eof7TcZ32oWpwIDAQAB";
	    //支付宝网关地址
//	    String GATEWAY = "https://openapi.alipay.com/gateway.do";
	    String GATEWAY = "https://openapi.alipaydev.com/gateway.do";
	    //付款结果回调
	    String PAY_NOTIFY_URL = HOST + CONTEXT_NAME + "/callback/alipay/pay_notify";
        
	    //退款回调
	    String REFUND_NOTIFY_URL = HOST + CONTEXT_NAME + "/callback/alipay/pay_refund";
	    //前台通知地址
	    String RETURN_URL = HOST + CONTEXT_NAME + "/notify/pay_success";
	    //参数类型
	    String PARAM_TYPE = "json";
	    //订单支付成功
	    String TRADE_SUCCESS = "TRADE_SUCCESS";
	    //交易关闭回调(当该笔订单全部退款完毕,则交易关闭)
	    //订单未付款交易超时关闭，或支付完成后全额退款
	    String TRADE_CLOSED = "TRADE_CLOSED";
	    //收款方账号
	    String SELLER_ID = "YOUR_SELLER_ID";
	}

}
