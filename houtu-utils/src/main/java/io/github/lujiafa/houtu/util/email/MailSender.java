package io.github.lujiafa.houtu.util.email;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @ClassName: MailSender
 * @author jonlu
 * @email lujiafayx@163.com
 * @date 2015年12月8日
 * @Description: 邮件发送工具类
 * 	也可以通过spring工具来实现，例如：
 * 	JavaMailSenderImpl s = new JavaMailSenderImpl();
 * 	s.setPassword(pwd);
 * 	s.setUsername(sender);
 * 	s.setProtocol("smtp");
 * 	s.setHost(host);
 * 	s.getJavaMailProperties().put("mail.smtp.auth", true);
 * 	s.getJavaMailProperties().put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
 * 	try {
 * 		s.testConnection();
 * 	} catch (MessagingException e) {
 * 		e.printStackTrace();
 * 	}
 */
public final class MailSender {

	private boolean debug = false; // 是否开启debug模式
	private boolean auth = false; // smtp是否需要认证
	private String protocol = "smtp"; // 邮件发送的协议
	private String contentType = "text/html;charset=UTF-8"; // 邮件内容类型
	private int port = -1; // 要连接的SMTP服务器的端口号，如果connect没有指明端口号就使用它，缺省值-1。
	private String host; // 设置SMTP主机服务名(e.g:smtp.163.com;smtp.qq.com;...)【必须】
	private String username; // smtp认证用户名【必须】
	private String password; // smtp认证密码/授权码【必须】
	private Properties properties = new Properties();
	private String subject; // EMAIL主题
	private String mailBody; // MAIL正文
	private String from; // 发送者EMAIL【必须】
	private String fromName; // 发送者EMAIL的名字（为空时自动匹配设置为发送者邮件名。e.g:from->lujiafayx@163.com则fromName->lujiafayx）
	private List<String> to = new ArrayList<String>(); // 收件人EMAIL【必须】
	private List<String> copyTo = new ArrayList<String>(); // 抄送EMAIL
	private List<String> covertTo = new ArrayList<String>(); // 密件抄送EMAIL
	private List<String> replyTo = new ArrayList<String>(); // 快捷回复EMAIL（当不设置此项时，默认为发送者EMAIL）
	private List<Object> attachment = new ArrayList<Object>(); // 附件
	
	public MailSender() {}
	
	public MailSender(String host, String from, String username, String password) {
		super();
		this.host = host;
		this.username = username;
		this.password = password;
		this.from = from;
	}
	
	public MailSender(String host, int port, String from, String username, String password) {
		super();
		this.port = port;
		this.host = host;
		this.username = username;
		this.password = password;
		this.from = from;
	}
	
	public void send() throws MessagingException, UnsupportedEncodingException {
		Properties props = new Properties(); // 属性对象
		props.put("mail.transport.protocol", protocol);
		props.put(String.format("mail.%s.auth", protocol), String.valueOf(auth));
		props.put("mail.debug", String.valueOf(debug));
		//props.put(String.format("mail.%s.socketFactory.class", protocol), "javax.net.ssl.SSLSocketFactory");
		if (this.properties.size() > 0) {
			props.putAll(this.properties);
		}
		Session session = Session.getInstance(props);
		MimeMessage message = new MimeMessage(session); // 创建MIME邮件对象
		// 配置发件人并添加到MIME邮件对象
		message.setFrom(new InternetAddress(from, fromName));
		// 配置收件对象数组并添加到MIME邮件对象
		Address[] recipients = new Address[to.size()];
		for (int i = 0; i < to.size(); i++) {
			InternetAddress address = new InternetAddress(to.get(i));
			recipients[i] = address;
		}
		message.setRecipients(Message.RecipientType.TO, recipients);
		// 配置抄送对象数组并添加到MIME邮件对象
		Address[] copyRecipients = new Address[copyTo.size()];
		for (int i = 0; i < copyTo.size(); i++) {
			InternetAddress address = new InternetAddress(copyTo.get(i));
			copyRecipients[i] = address;
		}
		message.setRecipients(Message.RecipientType.CC, copyRecipients);
		// 配置密件抄送对象数组并添加到MIME邮件对象
		Address[] covertRecipients = new Address[covertTo.size()];
		for (int i = 0; i < covertTo.size(); i++) {
			InternetAddress address = new InternetAddress(covertTo.get(i));
			covertRecipients[i] = address;
		}
		message.setRecipients(Message.RecipientType.BCC, covertRecipients);
		// 设置快捷回复对象数组并添加到MIME邮件对象（当此项为空时默认快捷回复对象为发送者）
		Address[] reply = new Address[replyTo.size()];
		for (int i = 0; i < replyTo.size(); i++) {
			InternetAddress address = new InternetAddress(replyTo.get(i));
			reply[i] = address;
		}
		message.setReplyTo(reply);
		message.setSubject(subject); // 设置主题
		// Multipart对象，包含邮件内容、标题、附件等内容
		Multipart multipart = new MimeMultipart();
		BodyPart bp = new MimeBodyPart(); // 邮件正文对象
		bp.setContent(mailBody, contentType);
		multipart.addBodyPart(bp);
		// 添加附件
		for (Object att : attachment) {
			DataSource dataSource = null;
			String fileName = null;
			if (att instanceof byte[]) {
				dataSource = new ByteArrayDataSource((byte[]) att, "application/octet-stream");
			} else if (att instanceof File) {
				File file = (File) att;
				dataSource = new FileDataSource(file);
				fileName = file.getName();
			} else if (att instanceof MimePart) {
				dataSource = new MimePartDataSource((MimePart) att);
			} else if (att instanceof CharSequence) {
				File file = new File(((CharSequence) att).toString());
				dataSource = new FileDataSource(file);
				fileName = file.getName();
			} else {
				throw new UnsupportedOperationException("Object types are not supported for the time being");
			}
			BodyPart attbp = new MimeBodyPart();
			attbp.setDataHandler(new DataHandler(dataSource));
			if (fileName != null) {
				attbp.setFileName(fileName);
			}
			multipart.addBodyPart(attbp);
		}
		message.setContent(multipart); // 将Multipart对象添加到MIME邮件对象
		message.saveChanges();
		// 获取通讯对象
		Transport transport = session.getTransport(protocol);
		transport.connect(host, port, username, password);
		transport.sendMessage(message, message.getAllRecipients());
		transport.close();
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	public void putPropertie(String key, Object val) {
		properties.put(key, val);
	}
	
	public Properties getProperties() {
		return properties;
	}

	public void setAuth(boolean auth) {
		this.auth = auth;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setMailBody(String mailBody) {
		this.mailBody = mailBody;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public void addTo(String recipient) {
		this.to.add(recipient);
	}

	public void addTo(List<String> to) {
		if (to != null)
			this.to.addAll(to);
	}

	public void addCopyTo(String copyRecipient) {
		this.copyTo.add(copyRecipient);
	}

	public void addCopyTo(List<String> copyTo) {
		if (copyTo != null)
			this.copyTo.addAll(copyTo);
	}

	public void addCovertTo(String covertRecipient) {
		this.covertTo.add(covertRecipient);
	}

	public void addCovertTo(List<String> covertTo) {
		if (covertTo != null)
			this.copyTo.addAll(covertTo);
	}

	public void addReplyTo(String reply) {
		this.replyTo.add(reply);
	}

	public void addReplyTo(List<String> replyTo) {
		if (replyTo != null)
			this.replyTo.addAll(replyTo);
	}

	public void addAttachment(Object att) {
		this.attachment.add(att);
	}

	public void addAttachment(List<Object> attachment) {
		if (attachment != null)
			this.attachment.addAll(attachment);
	}

	public void clearTo() {
		this.to.clear();
	}

	public void clearCopyTo() {
		this.copyTo.clear();
	}

	public void clearCovertTo() {
		this.covertTo.clear();
	}

	public void clearReplyTo() {
		this.replyTo.clear();
	}

	public void clearAttachment() {
		this.attachment.clear();
	}

}