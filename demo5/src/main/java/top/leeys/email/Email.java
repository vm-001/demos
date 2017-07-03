package top.leeys.email;


/**
 * 邮件的抽象父类
 * 
 * @author leeys.top@gmail.com
 */
public abstract class Email {
    private String address;
    
	public Email(String address) {
	    this.address = address;
	}
	
	public String getAddress() {
	    return address;
	}
	
	/*由子类实现的抽象方法*/
	public abstract String getTemplateName();
	
	public abstract String getSubTitle();
	
    public abstract String getText();

}
