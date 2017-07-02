package top.leeys.config;

/**
 * 全局配置
 * 
 * @author leeys.top@gmail.com
 */
public interface AppConfig {
    
    String CLASSPATH = AppConfig.class.getClassLoader().getResource("").getPath();
	
	/*开发者模式*/
	boolean DEVELOPER = true;
	
	interface JDBC {
		String driverClass = "com.mysql.jdbc.Driver";
		String database = "demo2";
	    String username = "";
	    String password = "";
	    String url = "jdbc:mysql://127.0.0.1:3306/"+ database +"?characterEncoding=UTF-8&createDatabaseIfNotExist=true";
	}
	
    interface GeeTest {
        String GEETEST_ID = "";
        String GEETEST_KEY = "";
    }
    
    /**
     * oauth流程:
     * 1)在html中引导用户访问超链接 
     * https://api.weibo.com/oauth2/authorize?response_type=code&client_id=292269826&redirect_uri=http%3A%2F%2Fdemo.leeys.top%2Fdemo2%2Foauth%2Fweibo%2Fcallback' (redirect_uri经过url编码)
     * 2)获取code
     * 用户授权后新浪会回调上面redirect_uri的值, 根据新浪传递的code对ACCESS_TOKEN_URL发起POST获取access_token
     * 3)获取用户信息
     * 对USER_INFO_URL发起GET获取用户信息
     */
	interface OAuth {
	    String REDIRECT_URL = "http://demo.leeys.top/demo2/oauth/weibo/callback";
	    
	    interface Github {
	        String TYPE = "github";
	        String CLIENT_ID = "";
	        String CLIENT_SECRET = "";
	        String STATE = "";
	    }
	    interface Weibo {
	        String TYPE = "weibo";
	        String APP_KEY = "Your APP_KEY"; 
	        String APP_SECRET = "Your APP_SECRET"; 
	        
	        String ACCESS_TOKEN_URL = "https://api.weibo.com/oauth2/access_token?client_id={APP_KEY}&client_secret={APP_SECRET}&grant_type=authorization_code&redirect_uri={REDIRECT_URL}&code={code}";
	        String USER_INFO_URL = "https://api.weibo.com/2/users/show.json?access_token={access_token}&uid={uid}";
	    }
	}
}
