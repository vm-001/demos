package top.leeys.exception;


/**
 * App全局异常基类
 * @author leeys.top@gmail.com
 *
 */
public class BaseException extends RuntimeException{
    private static final long serialVersionUID = 1L;
    private int code;
    private String message;
    
    public BaseException(int code, String message) {
        super();
        this.code = code;
        this.message = message;
    }
    
    public int code() {
        return code;
    }
    
    public String message() {
        return message;
    }
    
}
