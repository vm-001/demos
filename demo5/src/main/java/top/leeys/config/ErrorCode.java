package top.leeys.config;

/**
 * 错误代码
 * 
 * @author leeys.top@gmail.com
 */
public interface ErrorCode {
	int SUCCESS = 0;
	int PARAMS_ERROR = -1;     // 参数错误
	int NETWORK_ERROR = -2;    // 网络错误
	int SERVER_ERROR = -3;     // 服务端故障
	int UNKNOWN_ERROR = -4;    // 其他未知错误
	int LIMITED_AUTHORITY = -5;// 权限不足
	int NOT_SIGNIN = -6;
	
	/**
	 * ================== 注册(-2x)  ===================
	 */
	int EMAIL_ALREADY_EXIST = -29;    //
	int GEETEST_VALIDATE_ERROR = -28; //极验验证失败
	int EMAIL_ERROR = -27;			  //邮件格式错误
	int EMAIL_CODE_EXPIRED = -26;     //验证码过期       -> 重新注册
	int EMAIL_CODE_UNMATCHED = -25;   //验证码不匹配   -> 重新输入或重发邮件（这里还没实现重发邮件）
	/**
	 * =================邮件验证======================
	 */
	int EMAIL_ALREADY_ACTIVATE = -999; // 邮箱已经激活
	int EMAIL_TOKEN_EXPIRED = -998; // email_token 过期
	int EMAIL_NOT_EXIST = -997; // email不存在
	

	/**
	 * ================= 登录(-3x) ======================
	 */
	int USERNAME_OR_PASSWORD_ERROR = -37; //用户名或密码错误

}
