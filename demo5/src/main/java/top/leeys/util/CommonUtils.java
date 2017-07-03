package top.leeys.util;

import java.util.Random;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * 一般工具类
 * 
 * @author leeys.top@gmail.com
 */
public class CommonUtils {
	private static final char[] NUM = "0123456789".toCharArray();
	private static final Random random = new Random();
	private static final char[] CHARS = "0123456789abcdefghijklmnopqrstuvwsyzABCDEFGHIJKLMNOPQRSTUVWSYZ".toCharArray();
	
	/**
	 * 获取给定长度的随机数字[0-9]
	 */
	public static String getRandomDigits(int length) {
		char[] chars = new char[length];
		for (int i = 0; i < length; i++) {
			chars[i] = NUM[random.nextInt(NUM.length)];
		}
		return String.valueOf(chars); 
	}
	
	/**
	 * 获取给定长度的随机字符[0-9a-zA-Z]
	 */
	public static String getRandomCharacter(int length) {
	    char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = CHARS[random.nextInt(CHARS.length)];
        }
        return String.valueOf(chars); 
	}
	
	/**
	 * 将字符串md5加密两次
	 */
	public static String encodePassword(String str) {
		return CodecUtils.MD5(CodecUtils.MD5(str));
	}
	
	public static void setCookie(HttpServletResponse response, Cookie cookie) {
        cookie.setMaxAge(86400 * 30);
        response.addCookie(cookie);
	}
	
	public static void main(String[] args) {
	    System.out.println(encodePassword("woshiagou"));
		System.out.println(getRandomDigits(10));
		System.out.println("abcdefghijklmnopqrstuvwsyz".length());
		System.out.println(getRandomCharacter(20));
	}
	
}
