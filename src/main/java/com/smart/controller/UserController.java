package com.smart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserController {

	@RequestMapping("/index")
	public String dashboard() {
		System.out.println("Username: 'User'");
		return "normal/user_dashboard";
		
	}
	
}
