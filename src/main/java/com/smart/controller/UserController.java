package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
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
				Path path=Paths.get(file+File.separator+multipartFile.getOriginalFilename());
				Files.copy(multipartFile.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
				
				// image bkp of /static/img folder
				String file1="G:\\HotelSaharaStarBKP\\ImageUpload\\SCMImageUpload";
				Path path1=Paths.get(file1+File.separator+multipartFile.getOriginalFilename());
				Files.copy(multipartFile.getInputStream(), path1,StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is uploaded..");
			}
			else {
				System.out.println("File is empty..");
				contact.setImage("contact.png");
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
	
	//Showing particular contact details
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable ("cId") Integer cId, Model model, Principal principal) {
		
		String name=principal.getName();
		User user=userRepository.getUserByUserName(name);
		System.out.println("cId :: "+cId);
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact=contactOptional.get();
		if(user.getId()==contact.getUser().getId()) {
			model.addAttribute(contact);
			model.addAttribute("title",contact.getName());
		}
		return "normal/contact_detail";
	}

	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable ("cid") Integer cid, Model model, Principal principal, HttpSession session) {

		String name=principal.getName();
		User user=userRepository.getUserByUserName(name);
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();
		
		/*if(contact!=null && user.getId()==contact.getUser().getId()) {
			contact.setUser(null);
			this.contactRepository.delete(contact); // this delete is not working
			System.out.println("Deleted..");
			session.setAttribute("message", new Message("Contact deleted successfully..","success"));
		}*/ //below is simple way, we we want we can have service layer architecture and perform the same above operation, that will work
		if(contact!=null && user.getId()==contact.getUser().getId()) {
			user.getContacts().remove(contact);
			this.userRepository.save(user);
			System.out.println("Deleted..");
			session.setAttribute("message", new Message("Contact deleted successfully..","success"));
		}
		return "redirect:/user/show_contacts/0";
	}
	
	//open update form handler
	@PostMapping("/update-contact/{cid}")
	public String openUpdateForm(@PathVariable ("cid") Integer cid,Model model) {
		
		model.addAttribute("title","Update Contact");
		
		Contact contact= this.contactRepository.findById(cid).get();
		model.addAttribute("contact",contact);
		return "normal/update_form";
	}
	
	//update contact handler
	@PostMapping(value="/process-update")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile multipartFile,Principal principal,Model model,HttpSession session) {

		try {
			//image
			System.out.println("contact::"+contact);
			System.out.println("CID ::" +contact.getCid());
			//old contact details
			Contact oldContactDetails = this.contactRepository.findById(contact.getCid()).get();
			
			if(!multipartFile.isEmpty()){
				//file work  //rewrite
				//delete old photo
				if("contact.png"==oldContactDetails.getImage()) {
					File deletefile=new ClassPathResource("/static/img").getFile();
					File file1= new File(deletefile,oldContactDetails.getImage());
					file1.delete();
				}
				//update new photo
				File file=new ClassPathResource("/static/img").getFile();
				Path path=Paths.get(file+File.separator+multipartFile.getOriginalFilename());
				Files.copy(multipartFile.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(multipartFile.getOriginalFilename());
			}
			else {
				contact.setImage(oldContactDetails.getImage());
			}
			String name=principal.getName();
			User user=userRepository.getUserByUserName(name);
			
			contact.setUser(user);
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your contact is updated","success"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Contact Name ::" +contact.getName()+ "Contact Id :: "+contact.getCid());
		return "redirect:/user/"+contact.getCid()+"/contact";
	}
	
	//Your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {

		model.addAttribute("title","Profile Page");
		return "normal/profile";
	}
	
	//Open Setting Handler
	@GetMapping("/settings")
	public String openSettings() {
		return "normal/settings";
	}
	
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, Model model, Principal principal,HttpSession session) {
		
		System.out.println("oldPassword ::"+oldPassword+" , newPassword ::"+newPassword);
		
		String name=principal.getName();
		User user=userRepository.getUserByUserName(name);
		String currentPassword=user.getPassword();
		System.out.println("currentPassword :: "+currentPassword);
		
		if(this.passwordEncoder.matches(oldPassword, currentPassword)){
			//change password
			user.setPassword(this.passwordEncoder.encode(newPassword));
			this.userRepository.save(user);
			session.setAttribute("message",new Message("Your password is successfully changed..","success"));
		}
		else {
			//return with error message
			session.setAttribute("message",new Message("Please enter correct old password!!","danger"));
			return "redirect:/user/settings";
		}
		
		return "redirect:/user/index";
	}
}
