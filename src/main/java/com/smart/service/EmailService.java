package com.smart.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

	public boolean sendEmail(String subject, String message, String to) {
		
		boolean emailfl=false;
		String from="rahul.marathe123456@gmail.com";
		
		//variable for gmail
		String host="smtp.gmail.com";
		
		//get the system properties
		Properties properties=new Properties();
		//setting important information to properties object
		
		//host set
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");
		
		//Step 1: to get the session object
		Session session=Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("rahul.marathe123456@gmail.com", "Mokshita@6595");
				//return new PasswordAuthentication("rahulinfy5@gmail.com", "Rahul@12345");
				//If any exception occurred like invalid userid and password- plz check gmail setting 
				//goto->Manage your google account->security->less secure app access->ON 
			}
		});
		
		session.setDebug(true);
		
		//step2: compose the message [text, multimedia]
		MimeMessage m=new MimeMessage(session);
		
		try {
			m.setFrom(from);	//from email
			m.addRecipient(Message.RecipientType.TO, new InternetAddress(to)); //add recipient to message
			m.setSubject(subject); //adding subject to message
			//m.setText(message); //adding text to message
			m.setContent(message,"text/html");
			emailfl=true;
			Transport.send(m);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return emailfl;
	}
	
}
