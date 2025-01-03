package com.jsp.ekart.service;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestParam;

import com.jsp.ekart.dto.Customer;
import com.jsp.ekart.dto.Product;
import com.jsp.ekart.dto.Vendor;
import com.jsp.ekart.helper.AES;
import com.jsp.ekart.helper.EmailSender;
import com.jsp.ekart.repository.CustomerRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Component
public class CustomerService {
	
	@Autowired
	CustomerRepository repository3;
	
	@Autowired
	EmailSender emailSender;
	
	public String loadCustomerRegistration(ModelMap map,Customer customer) {
	    map.put("customer", customer);
	    return "customer-register.html";
	}

	public String customerRegistration(Customer customer,BindingResult result,HttpSession session) {
		if(!customer.getPassword().equals(customer.getConfirmPassword()))
		{
			result.rejectValue("confirmPassword","error.confirmPassword", "Password Doesnot match");
		}
		if(repository3.existsByEmail(customer.getEmail())){
			result.rejectValue("email","error.email","Email Already Exist");
		}
		if(repository3.existsByMobile(customer.getMobile())) {
			result.rejectValue("mobile","error.mobile","Mobile Number Already Exist");
		}
		if(result.hasErrors())
		{
			return "customer-register.html";
		}else {
			customer.setPassword(AES.encrypt(customer.getPassword()));
			int otp=new Random().nextInt(100000,1000000);
			customer.setOtp(otp);
			System.out.println(otp);
			emailSender.send(customer);
			repository3.save(customer);
			session.setAttribute("success", "Registration Done,Please Verify.. OTP sent");
			return "redirect:/customer/otp/"+customer.getId();
		}
	}

	public String verifyOtp(int id, int otp, HttpSession session) {
		Customer customer = repository3.findById(id).orElseThrow();
		if(customer.getOtp()==otp) {
			customer.setVerified(true);
			repository3.save(customer);
	     	session.setAttribute("success","Vendor account created successfully");
			return "redirect:/";
		}else {
			session.setAttribute("failure","Vendor account failed to create, OTP missmatch");
			return "redirect:/customer/otp/"+customer.getId();
		}
	}

	public String customerLogin(String email,String password,HttpSession session) {
		Customer customer=repository3.findByEmail(email);
		if(customer==null)
		{
			session.setAttribute("failure","Please Register first, no email was found");
			return "redirect:/customer/login";
		}else {
				if(AES.decrypt(customer.getPassword()).equals(password)) {
					if(customer.isVerified()) {
						session.setAttribute("success", "Account Logged in Successfully");
						return "redirect:/customer/home";
					}else {
						int otp=new Random().nextInt(100000,1000000);
						customer.setOtp(otp);
						emailSender.send(customer);
						repository3.save(customer);
						System.err.println(customer.getOtp());
						session.setAttribute("success","OTP sent successfully, Please verify now");
						return "redirect:/customer/otp/"+customer.getId();
					}
				}
				else {
					session.setAttribute("failure","Incorrect Password");
					return "redirect:/customer/login";
				}
		}
	}

	
}
