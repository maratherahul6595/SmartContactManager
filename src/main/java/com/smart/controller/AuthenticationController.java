package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

@Controller
public class AuthenticationController {

	Random random = new Random(1000);
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	//email form open handler
	@RequestMapping("/forgot")
	public String openEmailForm() {

		return "forgot_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam ("email") String email, HttpSession session) {

		try {
			System.out.println("Email :: "+email);
			//1. generate OTP of 4 digit 
			int otp = random.nextInt(999999);
			System.out.println("OTP ::"+otp);

			//2. send otp to user
			String subject="OTP from SCM";
			String message="<div style='border:1px solid #e2e2e2; padding:20px'>"
							+"<h3>"
							+"Please use below OTP for SCM verification. OTP is "
							+"<br>"
							+"<b>"
							+otp
							+"</b>"
							+"</h3>"
							+"</div>";
			String to=email;
			boolean flag = this.emailService.sendEmail(subject, message, to);
			System.out.println("Flag value is ::"+flag);
			if(flag) {
				session.setAttribute("myotp", otp);
				session.setAttribute("email", email);
				
				//session.setAttribute("message", "We have sent OTP to your email..");
				session.setAttribute("message",new Message("We have sent OTP to your email..","success"));
				return "verify_otp";
			}else {
				//session.setAttribute("message", "Check your email id !!");
				session.setAttribute("message",new Message("Check your email id !!","danger"));
				return "forgot_email_form";
			}
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message",new Message("Check your email id !!","danger"));
			return "forgot_email_form";
		}
	}
	
	//verify otp
	@PostMapping("/verify-otp")
	public String verifyOTP(@RequestParam ("otp") int otp, HttpSession session) {
		
		int myotp=(int) session.getAttribute("myotp");
		String email=(String) session.getAttribute("email");
		if(myotp==otp) {
			//password change form
			User user = this.userRepository.getUserByUserName(email);
			
			if(user==null) {
				//send error message
				session.setAttribute("message", "User doesnot exist with this email !!");
				return "forgot_email_form";
			}else {
				//send change password form	
				return "password_change_form";
			}
			
		}else {
			//session.setAttribute("message", "You have entered wrong OTP!!");
			session.setAttribute("message",new Message("You have entered wrong OTP!!","danger"));
			return "verify_otp";
		}
	}
	
	//change password
	@PostMapping("/update-password")
	public String changePassword(@RequestParam ("newpassword") String newpassword, HttpSession session) {
		
		String email=(String) session.getAttribute("email");
		User user=this.userRepository.getUserByUserName(email);
		user.setPassword(this.passwordEncoder.encode(newpassword));
		this.userRepository.save(user);
		return "redirect:/signin?changepassword=Passoword changed successfully..";
	}
}
