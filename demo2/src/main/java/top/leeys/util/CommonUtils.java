package top.leeys.util;

import java.util.Random;
import java.util.UUID;

/**
 * 一般工具类
 * 
 * @author leeys.top@gmail.com
 */
public class CommonUtils {
    private static final Random random = new Random();
	private static final char[] NUM = "0123456789".toCharArray();
	/*随机产生字符*/
    private static final char[] chars = {
        '0','1','2','3','4','5','6','7','8','9',
        'a','b','c','d','e','f','g','h','i','j',
        'k','l','m','n','o','p','q','r','s','t',
        'u','v','w','s','y','z',
        'A','B','C','D','E','F','G','H','I','J',
        'K','L','M','N','O','P','Q','R','S','T',
        'U','V','W','S','Y','Z',
    };
    
	/**
	 * 获取位数指定的随机数字
	 */
	public static String getRandomDigits(int length) {
		char[] chars = new char[length];
		for (int i = 0; i < length; i++) {
			chars[i] = NUM[random.nextInt(NUM.length)];
		}
		return String.valueOf(chars); 
	}
	
    /**
     * 获得格式化的uuid
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
    
    /**
     * 获取10位随机字符(比如:9V72aQgNwI)
     */
    public static String getToken() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(chars[random.nextInt(chars.length)]);
        }
        return sb.toString();
    }
}
