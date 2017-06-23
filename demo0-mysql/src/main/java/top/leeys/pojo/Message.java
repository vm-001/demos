package top.leeys.pojo;

/**
 * 错误信息处理
 * 服务器返回客户端的json格式
 * {
 *   "code":$code,
 *   "message": $message,
 *   "content":{}
 * }
 * @author jiekechoo
 *
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

}
