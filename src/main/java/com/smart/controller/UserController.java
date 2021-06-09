package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
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
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile multipartFile, Principal principal,HttpSession session) {
		
		try {
			String name=principal.getName();
			User user=userRepository.getUserByUserName(name);
			
			contact.setUser(user);
			user.getContacts().add(contact);
			
			//Processing and uploading image file
			if(!multipartFile.isEmpty()) {
				contact.setImage(multipartFile.getOriginalFilename());
				File file=new ClassPathResource("/static/img").getFile();
				Path path=Paths.get(file.getAbsolutePath()+File.separator+multipartFile.getOriginalFilename());
				Files.copy(multipartFile.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is uploaded..");
			}
			else {
				System.out.println("File is empty..");
			}
			
			this.userRepository.save(user);
			System.out.println("User :: "+user);
			System.out.println("Data ::"+contact);
			System.out.println("User Details Added Successfully..");
			
			session.setAttribute("message", new Message("Your contact is added!! add more..","success"));
		} catch (Exception e) {
			System.out.println("ERROR :: "+e.getMessage());
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong!! try again..","danger"));
		}
		return "normal/add_contact_form";
	}
	
	//View Contacts handler
	//per page =5[n]
	//current page = 0[page]
		@GetMapping("/show_contacts/{page}")
		public String viewContactsPage(@PathVariable ("page") Integer page, Model model, Principal principal) {
			model.addAttribute("title","View Contacts");
			
			String name=principal.getName();
			User user=userRepository.getUserByUserName(name);
			Pageable pageable=PageRequest .of(page, 3);
			
			Page<Contact> contacts=this.contactRepository.findContactsByUser(user.getId(),pageable);
			
			model.addAttribute("contacts",contacts);
			model.addAttribute("currentPage",page);
			model.addAttribute("totalPages",contacts.getTotalPages());
			return "normal/show_contacts";
		}	
}
