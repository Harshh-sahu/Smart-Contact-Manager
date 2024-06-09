package com.smart.controllers;

import java.io.File;
import java.nio.channels.NonReadableChannelException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import com.smart.helper.Message;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.objenesis.instantiator.basic.NewInstanceInstantiator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.MyOrderRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.MyOrder;
import com.smart.entities.User;

import jakarta.servlet.http.HttpSession;

import java.nio.file.Path;
import com.razorpay.*;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository userRepository;
@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	public PasswordEncoder encoder;
	@Autowired
	private ContactRepository contactRepository;
@Autowired
	private MyOrderRepository myOrderRepository;
	
	
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "User Dashboard");

		return "normal/user_dashboard";
	}

	// method for adding common data tto response

	@ModelAttribute
	public void addCommonData(Model m, Principal principal) {
		String userName = principal.getName();
		System.out.println("USERNAME = " + userName);

		User user = this.userRepository.getUserByUserName(userName);
		System.out.println("user : = " + user);

		m.addAttribute("user", user);
		// get the user using username(email);
	}

	// open ADD FORM HANDLER
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	// processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {

		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

			// Processing and uploading file
			if (file.isEmpty()) {
				System.out.println("file is empty");
				contact.setImage("contact.png");
			} else {
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("image is uploaded");
			}

			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);

			System.out.println("added to database");
			System.out.println("data" + contact);

			// Message on success
			session.setAttribute("message", new Message("Your Contact is added !! Add more", "success"));

		} catch (Exception e) {
			System.out.println("ERROR" + e.getMessage());
			e.printStackTrace();

			// Error message
			session.setAttribute("message", new Message("Something went wrong! Try again", "danger"));
		}

		return "normal/add_contact_form";
	}

	// show contacts handler
	// per page 5 contact
	// current page = 0;[page]

	@GetMapping("/show-contacts/{page}")
	public String showContact(@PathVariable("page") Integer page, Model m, Principal principal) {
		m.addAttribute("title", "Show User Contacts");
		// contact ki list ko bhejni hai
		String userName = principal.getName();

		User user = this.userRepository.getUserByUserName(userName);
		Pageable pageable = PageRequest.of(page, 8);
		Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(), pageable);
		m.addAttribute("currentPage", page);
		m.addAttribute("contacts", contacts);
		m.addAttribute("totalPages", contacts.getTotalPages());
		return "normal/show_contacts";
	}

	// showing specific contact detail

	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);

		Contact contact = contactOptional.get();

		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		if (user.getId() == contact.getUser().getId()) {

			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());

		}

		System.out.println("cId" + cId);
		return "normal/contact_detail";
	}

	
	
	//delete Contact Handler
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId,Principal principal,HttpSession session) {
		
		Optional<Contact>   contactOptional=  this.contactRepository.findById(cId);
		
	Contact contact=	contactOptional.get();

	String userName = principal.getName();
	User user = this.userRepository.getUserByUserName(userName);
	//check......Assignment
	if (user.getId() == contact.getUser().getId()) {
       		
		this.contactRepository.delete(contact);
             session.setAttribute("message",new Message("contact deleted successfully !!","success"));
	}

		return "redirect:/user/show-contacts/0";
	}
	
	
	//open update form handler
	   @PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId,Model m) {
	    
		   m.addAttribute("title","Update Contact");
		   
		Contact contact =  this.contactRepository.findById(cId).get();
		m.addAttribute("contact",contact);
		return "normal/update_form";
		
	}
	   
	   
	   
	   //update CONTACTS FORM HANDLER
	   @PostMapping("/process-update")
	   public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
	                               Model m, HttpSession session, Principal principal) {
	       try {
	           // Retrieve existing contact from the database
	           Contact existingContact = this.contactRepository.findById(contact.getcId()).get();

	           // Handle new profile image upload
	           if (!file.isEmpty()) {
	               // Delete old image
	               File deleteFile = new ClassPathResource("static/img").getFile();
	               Path deletePath = Paths.get(deleteFile.getAbsolutePath() + File.separator + existingContact.getImage());
	               Files.deleteIfExists(deletePath);

	               // Save new image
	               existingContact.setImage(file.getOriginalFilename());
	               File saveFile = new ClassPathResource("static/img").getFile();
	               Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
	               Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
	           } else {
	               // Keep the existing image if no new image is uploaded
	               contact.setImage(existingContact.getImage());
	           }

	           // Update other contact details
	           existingContact.setName(contact.getName());
	           existingContact.setSecondName(contact.getSecondName());
	           existingContact.setPhone(contact.getPhone());
	           existingContact.setEmail(contact.getEmail());
	           existingContact.setWork(contact.getWork());
	           existingContact.setDescription(contact.getDescription());

	           // Set the user
	           User user = this.userRepository.getUserByUserName(principal.getName());
	           existingContact.setUser(user);

	           // Save updated contact
	           this.contactRepository.save(existingContact);
	           session.setAttribute("message", new Message("Contact updated successfully!", "success"));
	       } catch (Exception e) {
	           session.setAttribute("message", new Message("Something went wrong! Try again.", "danger"));
	           e.printStackTrace();
	       }

	       return "redirect:/user/" + contact.getcId() + "/contact";
	   }
	   
	   //your profile handler
	   @GetMapping("/profile")
	   public String yourProfile(Model model) {
		   
		   
		   model.addAttribute("title","profile page");
		   return "normal/profile";
	   }
	   
	   
	   
	 //open update form handler
	   @PostMapping("/update-user/{id}")
	public String updateUserForm(@PathVariable("id") Integer id,Model m) {
	    System.out.println("working");
		   m.addAttribute("title","Update User");
		   
		   User user = this.userRepository.findById(id).get();
		m.addAttribute("user",user);
		return "normal/update_user_form";
		
	}
	   
	   
	   
	   //update CONTACTS FORM HANDLER
	// update CONTACTS FORM HANDLER
	   @PostMapping("/process-user-update")
	   public String updateUserHandler(@Valid @ModelAttribute User user, BindingResult result,
	                                   @RequestParam("profileImage") MultipartFile file,
	                                   Model m, HttpSession session, Principal principal) {
	       if (result.hasErrors()) {
	           // Handle validation errors
	           m.addAttribute("user", user);
	           return "normal/update_user_form";
	       }

	       try {
	           // Retrieve existing user from the database
	           User existingUser = this.userRepository.findById(user.getId())
	                                                  .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + user.getId()));

	           // Handle new profile image upload
	           if (!file.isEmpty()) {
	               // Delete old image
	               File deleteFile = new ClassPathResource("static/img").getFile();
	               Path deletePath = Paths.get(deleteFile.getAbsolutePath() + File.separator + existingUser.getImageUrl());
	               Files.deleteIfExists(deletePath);

	               // Save new image
	               existingUser.setImageUrl(file.getOriginalFilename());
	               File saveFile = new ClassPathResource("static/img").getFile();
	               Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
	               Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
	           } else {
	               // Keep the existing image if no new image is uploaded
	               user.setImageUrl(existingUser.getImageUrl());
	           }

	           // Update other user details
	           existingUser.setName(user.getName());
	           existingUser.setEmail(user.getEmail());
	           existingUser.setAbout(user.getAbout());

	           // Save updated user
	           this.userRepository.save(existingUser);
	           session.setAttribute("message", new Message("User updated successfully!", "success"));
	       } catch (Exception e) {
	           session.setAttribute("message", new Message("Something went wrong! Try again.", "danger"));
	           e.printStackTrace();
	       }

	       return "redirect:/user/profile";
	   }

//forget password field open setting handler
	   
	     @GetMapping("/settings") 
	   public String openSettings() {
		   return "normal/settings";
	   }
	   
	     //change password
	     @PostMapping("/change-password")
	     public String changePassword(@RequestParam("oldPassword")String oldPassword,@RequestParam("newPassword")String newPassword,Principal principal,HttpSession httpSession) {
	    	 System.out.println(oldPassword);
	    	 System.out.println(newPassword);
	    	 
	    	String userName= principal.getName();
	    	  User currentUser= this.userRepository.getUserByUserName(userName);
	    	   if(this.bCryptPasswordEncoder.matches( oldPassword , currentUser.getPassword())) {
	    		   //change the password
	    		   
	    		   currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
	    		   this.userRepository.save(currentUser);
	    		   httpSession.setAttribute("message",new Message("your password has successfully changed", "success") );
	    	   }else{
	    		   //error
	    		   httpSession.setAttribute("message",new Message("your old password was wrong", "danger") );
	  	    	 return "redirect:/user/settings";

	    	   }
	    	   
	    	    
	    	 return "redirect:/user/index";
	     }
	     
	     //creating order for payment 
	     
	     @PostMapping("/create_order")
	     @ResponseBody
	  public String createOrder(@RequestBody Map<String, Object> data,Principal principal) throws Exception {
	    	 
//	    	 System.out.println("ORDER FUNCTION EXECUTED");
	    	 System.out.println(data);
	    int amt=Integer.parseInt( data.get("amount").toString());	
	    var client=  new RazorpayClient("rzp_test_vK7txoJKcpRCeT","RfYK1IxfamHF7x24xgnM2jlN");
	    
	    JSONObject ob = new JSONObject();  
	    ob.put("amount", amt*100);
	    ob.put("currency", "INR");
	    ob.put("receipt", "txn_235425");
	    
	    //creating a order
	    
	    Order order = client.orders.create(ob);
	    System.out.println(order);
	    
	    //save the order in the database
	     
	    MyOrder myOrder = new MyOrder();
	    
	    myOrder.setAmount(order.get("amount")+"");
	    myOrder.setOrderId(order.get("id"));
	    myOrder.setPaymentId(null);
	    myOrder.setStatus("created");
	    myOrder.setUser(this.userRepository.getUserByUserName(principal.getName()));
	    
	    myOrder.setReceipt(order.get("receipt"));
	    
	    this.myOrderRepository.save(myOrder);
	    
	    //if you woant to save the information to database
	    
		  return order.toString();
	  }
	     
	     @PostMapping("/update_order")
	     public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data){	
	         System.out.println("Data received: " + data);
	         
	         MyOrder myorder = this.myOrderRepository.findByOrderId(data.get("order_id").toString());
	         if (myorder == null) {
	             return ResponseEntity.badRequest().body(Map.of("msg", "Order not found"));
	         }
	         
	         myorder.setPaymentId(data.get("payment_id").toString());
	         myorder.setStatus(data.get("status").toString());
	         this.myOrderRepository.save(myorder);
	         
	         System.out.println("Updated order: " + myorder);
	         
	         return ResponseEntity.ok(Map.of("msg","updated"));
	     }

}
