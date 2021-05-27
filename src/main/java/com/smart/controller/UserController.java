package com.smart.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.smart.dao.UserRepository;
import com.smart.entities.User;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;
	
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		
		String username=principal.getName();
		System.out.println("Username :: "+username);
		
		User user=userRepository.getUserByUserName(username);
		System.out.println("User :: "+user);
		
		model.addAttribute(user);
		
		System.out.println("Username: 'User' is default in spring boot..");
		
		return "normal/user_dashboard";
		
	}
	
}
