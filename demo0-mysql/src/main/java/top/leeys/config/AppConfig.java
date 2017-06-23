package top.leeys.config;

/**
 * @author leeys.top@gmail.com
 *
 */
public interface AppConfig {
    /*项目的classpath*/
    String CLASSPATH = AppConfig.class.getClassLoader().getResource("").getPath();
    
	/*开发者模式*/
	boolean DEVELOPER = true;
	
	interface JDBC {
	    String url = "jdbc:mysql://127.0.0.1:3306/demo0?characterEncoding=utf-8&createDatabaseIfNotExist=true";
	    String username = "root";
	    String password = "";
	}
	
	/**
	 * html距今有一年历史, 没有打算重构增geetest。
	 */
	interface GeeTest {
		String GEETEST_ID = "7c25da6fe21944cfe507d2f9876775a9";
		String GEETEST_KEY = "f5883f4ee3bd4fa8caec67941de1b903";
	}
}
