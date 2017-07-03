package top.leeys.pojo;

/**
 * {
 *   "code":$code,
 *   "message": $message,
 *   "content":{}
 * }
 */
public class Message {
    
	private int code;
	private String message;
	private Object content;

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public Object getContent() {
		return content;
	}
	
	public Message() {}

	public Message(int code, String message) {
		this(code, message, "no content");
	}

	public Message(int code, String message, Object content) {
		this.code = code;
		this.message = message;
		this.content = content;
	}

	public void setMsg(int code, String message) {
		this.code = code;
		this.message = message;
		this.content = "no content";
	}

	public void setMsg(int code, String message, Object content) {
		this.code = code;
		this.message = message;
		this.content = content;
	}

	
	public static Message ok() {
	    return new Message(0, "success");
	}
	
	public static Message ok(Object obj) {
	    return new Message(0, "success", obj); 
	}
	
	public static Message notFound() {
	    return new Message(404, "not found");
	}
	
	public static Message notFound(String msg) {
        return new Message(404, msg);
    }
}
