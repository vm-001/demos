package top.leeys.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CodecUtils {
	private static MessageDigest md5;
	static {
		try {
			md5 = MessageDigest.getInstance("MD5");
		} 
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public static String MD5(String str) {
		md5.update(str.getBytes());
		byte[] b = md5.digest();
		int i;
		StringBuilder sb = new StringBuilder();
		for (int offset = 0; offset < b.length; offset++) {
			i = b[offset];
			if (i < 0)
				i += 256;
			if (i < 16)
				sb.append("0");
			sb.append(Integer.toHexString(i));
		}
		return sb.toString();
	}
	

    public static String base64Encode(String str) {
        String encodedstr = new String(Base64.encodeBase64(str.getBytes(Charsets.UTF_8), false), Charsets.UTF_8);
        return encodedstr;
    }
    
    /**
     * 
     * @param 如果为null 则返回 null
     * @return
     */
    public static String base64Decode(String str) {
    	String decodedstr = null;
    	if (str != null)
    		decodedstr = new String(Base64.decodeBase64(str.getBytes(Charsets.UTF_8)), Charsets.UTF_8);
    	return decodedstr;
    }
    
    public static String URLEncode(String str) {
    	String encodedstr = null;
    	try {
			encodedstr = URLEncoder.encode(str, "utf-8");
		} catch (UnsupportedEncodingException e) {
			log.error("不支持utf-8编码");
			e.printStackTrace();
		}
    	return encodedstr;
    }
    
    public static String URLDecode(String str) {
    	String decodedstr = null;
    	try {
    		decodedstr = URLDecoder.decode(str, "utf-8");
		} catch (UnsupportedEncodingException e) {
			log.error("不支持utf-8编码");
			e.printStackTrace();
		}
    	return decodedstr;
    }
    
    public static void main(String[] args) {
        System.out.println(MD5("123"));
        System.out.println(URLEncode("leeys.top@gmail.com"));
        System.out.println(URLDecode("leeys.top%40gmail.com"));
	}
}
