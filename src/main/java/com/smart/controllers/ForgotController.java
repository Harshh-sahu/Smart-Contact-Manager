package com.smart.controllers;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
    @Autowired
	  private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private EmailService emailService;

	@Autowired
	private UserRepository userRepository;

	// email id form open handler
	@GetMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}

	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email, HttpSession httpSession) {
		System.out.println(email);

		// generating otp of six digits
		Random random = new Random();
		int otp = 100000 + random.nextInt(900000); // This will generate a 6-digit OTP

		System.out.println(otp);

		// write code for sending email
		String subject = "OTP FROM SMART CONTACT MANAGER";
		String message = "" + "<div style='border:1px solid #e2e2e2; padding:20px'>" + "<h1>" + "OTP is  " + "<b>" + otp
				+ "</b></h1>" + "</div>";
		String to = email;
		boolean flag = this.emailService.sendEmail(subject, message, to);
		if (flag) {
			httpSession.setAttribute("myotp", otp);
			httpSession.setAttribute("email", email);
			return "verify_otp";
		} else {
			httpSession.setAttribute("message", "Check your email ID!");
			return "forgot_email_form";
		}
	}

	// verify otp
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") Integer otp, HttpSession httpSession) {
		Integer myOtp = (Integer) httpSession.getAttribute("myotp");
		String email = (String) httpSession.getAttribute("email");

		if (myOtp != null && myOtp.equals(otp)) {
			// Password change form
			User user = this.userRepository.getUserByUserName(email);
			if (user == null) {
				// send error message
				httpSession.setAttribute("message", "USer does not exit with these email!");
				return "forgot_email_form";
			} else {
				// send change password form

			}
			return "password_change_form";
		} else {
			httpSession.setAttribute("message", "You have entered the wrong OTP");
			return "verify_otp";
		}
	}
	
	//chanfe password
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword,HttpSession httpSession) {
		String email = (String) httpSession.getAttribute("email");
    User user= this.userRepository.getUserByUserName(email);
    user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
    this.userRepository.save(user);
   
	  return "redirect:/signin?change=password changed successfully";
	}
}
