package top.leeys.util;

/**
 * 自定义验证
 * @author leeys.top@gmail.com
 *
 */
public class CustomValidator {
	private static final String REG_EMAIL = "^\\w+([-+.]\\w+)*@\\w+\\.((com|cn)|(com.cn))$";
	
    public static boolean hasEmpty(String...strings){
        for (String str:strings){
            if (str==null||str.isEmpty()){
                return true;
            }
        }
        return false;
    }
    public static boolean hasNull(Object...objects){
        for (Object object:objects){
            if(object==null){
                return true;
            }
        }
        return false;
    }
    
    public static boolean isEmail(String email) {
        if (email == null)
            return false;
    	return email.matches(REG_EMAIL);
    }
    
//    public static TokenVerifyResult JwtVerify(final String key, final String jwt){
//        try {
//            Jwts.parser()
//                    .setSigningKey(DatatypeConverter.parseBase64Binary(key))
//                    .parseClaimsJws(jwt);
//        } catch (ExpiredJwtException e) {
//            return TokenVerifyResult.expired;
//        } catch (SignatureException e) {
//            return TokenVerifyResult.illegalSignature;
//        }
//        return TokenVerifyResult.success;
//    }
//
//    public static boolean checkConfirmMessage(Message message){
//        // TODO: 16-12-18  没有实现验证确认交易的Message方法
//        if (message==null){
//            return false;
//        }
//        return true;
//    }
    
    public static void main(String[] args) {
    	System.out.println(CustomValidator.hasEmpty("a",null));
    	System.out.println(isEmail(null));
    }
}
