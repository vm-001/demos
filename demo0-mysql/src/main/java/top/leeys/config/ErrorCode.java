package top.leeys.config;

/**
 * 错误代码 
 */
public interface ErrorCode {
	int SUCCESS = 0;
	int PARAMS_ERROR = -1;     // 参数错误
	int NETWORK_ERROR = -2;    // 网络错误
	int SERVER_ERROR = -3;     // 服务端故障
	int UNKNOWN_ERROR = -4;    // 其他未知错误
	int LIMITED_AUTHORITY = -5;// 权限不足
	
	
	int SID_NOT_EXIST = -11;  
	int PASSWORD_ERROR = -12;
	int IDCARD_ERROR = -13;
	int NAME_ERROR = -14;  
	int SESSION_EXPIRED = -15;
	int USER_ALREADY_SELECT = -16;  //已选课
	int COURSE_IS_ZERO = -17;
}
