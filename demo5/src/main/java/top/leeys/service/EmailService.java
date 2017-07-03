package top.leeys.service;

import static top.leeys.config.AppConfig.Email.FROM;
import static top.leeys.config.AppConfig.Email.HOST;
import static top.leeys.config.AppConfig.Email.TITLE;
import static top.leeys.config.AppConfig.Email.TOKEN;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.sun.mail.smtp.SMTPSendFailedException;
import com.sun.mail.util.MailSSLSocketFactory;

import lombok.extern.slf4j.Slf4j;
import top.leeys.config.AppConfig;
import top.leeys.email.Email;

@Service
@Slf4j
public class EmailService {
    
	private static final String EMAIL_DIR = AppConfig.CLASSPATH + "email_templates";
    
	private static Map<String, String> templatesMap = new HashMap<>();
	
	private static Properties props = new Properties();
	private static Session session;
	private static Transport transport;

	public EmailService() {
//        props.setProperty("mail.debug", "true");              //开启debug调试
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.host", HOST);          //设置邮件服务器主机名
        props.setProperty("mail.transport.protocol", "smtp");   //发送邮件协议名称

		MailSSLSocketFactory sf = null;
		try {
			sf = new MailSSLSocketFactory();
		} catch (GeneralSecurityException e) {
			;
		}
		//SSL
		sf.setTrustAllHosts(true);
		props.put("mail.smtp.ssl.enable", "true");
		props.put("mail.smtp.ssl.socketFactory", sf);

		session = Session.getInstance(props);
		try {
			transport = session.getTransport();
			validateConnect();
		} catch (NoSuchProviderException e) {
		    ;
		}
		
		loadEmailTemplate();
	}

	public static String getEmailTemplate(String name) {
		return templatesMap.get(name);
	}
	
	
	@Async
	public void sendEmail(Email email) {
	    Message message = new MimeMessage(session);
	    int retry = 0;
	    //重试3次
	    while (retry < 3) {
		    try {
		    	message.setSubject(TITLE + email.getSubTitle());   //标题
		    	message.setContent(email.getText(), "text/html;charset=UTF-8"); //文本
				message.setFrom(new InternetAddress(FROM)); //发件人
		    	sendEmail(message, email.getAddress());
		    	log.debug("邮件已发送至:" + email.getAddress());
		    	return;
		    } catch (SMTPSendFailedException e) {
		        /*
	             * 因为腾讯的限制，当邮件发送太快时会出现SMTPSendFailedException继承自MessagingException
	             * 550 Connection frequency limited
	             * http://service.mail.qq.com/cgi-bin/help?subtype=1&id=20022&no=1000722
	             */
		        e.printStackTrace();
		        log.error("邮件发送失败：{}, 原因：{}", email.getAddress(), e.getMessage());
		    } catch (Exception e) {
		        e.printStackTrace();
		    	log.error("邮件发送失败：{}, 原因：{}", email.getAddress(), e.getMessage());
		    }
		    try {
                Thread.sleep(1000);  //休眠1秒
            } catch (InterruptedException e1) {  
                //https://www.ibm.com/developerworks/cn/java/j-jtp05236.html
                log.error("线程中断异常", e1);
            }
	        retry++;
	        log.debug("重试：" + retry);
	    }
        log.error("邮件发送失败: {}", email.getAddress());
	}
	
	
	private void sendEmail(Message message, String address) throws AddressException, MessagingException {
	    validateConnect();
    	transport.sendMessage(message, new Address[] { new InternetAddress(address) });
	}
	
	private void validateConnect() {
	    if (!transport.isConnected()) {
            try {
                transport.connect(HOST, FROM, TOKEN);
            } catch (MessagingException e) {
                log.error("Email服务器异常");
            }
	    }
	}
	
	
	/**
	 * 从文件夹中缓存html模板到Map内存中
	 */
	private void loadEmailTemplate() {
		File files = new File(EMAIL_DIR);
		FileInputStream in = null;
		BufferedReader buf = null;
		for (File file : files.listFiles()) {
			try {
				in = new FileInputStream(file);
				buf = new BufferedReader(new InputStreamReader(in, "UTF-8"));
				String str = "";
				String line = "";
				while ((line = buf.readLine()) != null) {
					str += line.trim();
				}
				templatesMap.put(file.getName(), str);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					in.close();
					buf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}
//        for (String key : templatesMap.keySet()) {
//            System.out.println(key + ":" + templatesMap.get(key));
//		}
		System.out.println("邮件模板已加载好");
	}

	
}
