package top.leeys.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CommonUtil {

    private static MessageDigest md5;
    static {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            ;
        }
    }

    public static String md5(String str) {
        String result = null;
        try {
            md5.update(str.getBytes("UTF-8")); //throw NullPointerException when str is null
            result = new BigInteger(1, md5.digest()).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        System.out.println(md5(null));
    }

}
