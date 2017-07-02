package top.leeys.config;


/**
 * 全局错误码
 *  
 * @author leeys.top@gmail.com
 */
public interface ErrorCode {
	int SUCCESS = 0;
	int PARAMS_ERROR = -1;     
	int NETWORK_ERROR = -2;
	int SERVER_ERROR = -3;
	int UNKNOWN_ERROR = -4; 
	int LIMITED_AUTHORITY = -5;//
	
	int TOKEN_ERROR = -19;
	int USER_NOT_FOUND = -18;
	int RATE_LIMITED = -17;
	
	/* socket */
	int USER_DISCONNECTED = -29;
	int INVITER_IN_GAME = -28;  //主邀方正在游戏
}
