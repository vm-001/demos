package top.leeys.exception;


/**
 * 应用全局异常的父类，用于处理应用抛出的异常
 * @author leeys.top@gmail.com
 *
 */
public class BaseException extends RuntimeException{
    private static final long serialVersionUID = 1L;
    private int code;
    private String message;
    
    public BaseException(int code, String message) {
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
