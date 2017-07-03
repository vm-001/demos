package top.leeys.email;

import top.leeys.service.EmailService;

/**
 * 验证邮箱的邮件
 * 
 * @author leeys.top@gmail.com
 */
public class SignupEmail extends Email{
    private static final String TEMPLATE_NAME = "signup.html";
    private static final String SUBTITLE = "验证邮箱";
    
	private String template;
	private String url;
	
	public SignupEmail(String address, String url) {
	    super(address);
		this.url = url;
		this.template = EmailService.getEmailTemplate(TEMPLATE_NAME);
	}
	
	 
	@Override
	public String getText() {
		String result = template.replaceAll("%url", url);
		return result;
	}

    @Override
    public String getTemplateName() {
        return TEMPLATE_NAME;
    }

    @Override
    public String getSubTitle() {
        return SUBTITLE;
    }
	
}
