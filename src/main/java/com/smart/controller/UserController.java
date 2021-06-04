package com.smart.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;
	
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String username=principal.getName();
		System.out.println("Username :: "+username);
		
		User user=userRepository.getUserByUserName(username);
		System.out.println("User :: "+user);
		
		model.addAttribute(user);
		
		System.out.println("Username: 'User' is default in spring boot..");
	}
	
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}

	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		
		return "normal/add_contact_form";
	}
	
	//processing add contact
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, Principal principal) {
		
		String name=principal.getName();
		User user=userRepository.getUserByUserName(name);
		
		contact.setUser(user);
		user.getContacts().add(contact);
		this.userRepository.save(user);
		System.out.println("User :: "+user);
		System.out.println("Data ::"+contact);
		System.out.println("User Details Added Successfully..");
		return "normal/add_contact_form";
	}
}
