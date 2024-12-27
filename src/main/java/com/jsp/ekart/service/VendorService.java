package com.jsp.ekart.service;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;

import com.jsp.ekart.dto.Vendor;
import com.jsp.ekart.helper.AES;
import com.jsp.ekart.helper.EmailSender;
import com.jsp.ekart.repository.VendorRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Service
public class VendorService {
	
	@Autowired
	VendorRepository repository;
	
	@Autowired
	EmailSender emailSender;
	
	public String loadRegistration(ModelMap map,Vendor vendor)
	{
		map.put("vendor", vendor);
		return "vendor-registration.html";
	}
	
	public String vendorRegistration(Vendor vendor,BindingResult result,HttpSession session) {
		if(!vendor.getPassword().equals(vendor.getConfirmPassword())) {
			result.rejectValue("confirmPassword","error.confirmPassword","* Password and Confirm Password Should Match");
		}
		if(repository.existsByEmail(vendor.getEmail())) {
			result.rejectValue("email","error.email","* Email already exist");
		}
		if (repository.existsByMobile(vendor.getMobile()))
			result.rejectValue("mobile", "error.mobile", "* Mobile Number Already Exists");
		if (result.hasErrors()) {
			return "vendor-registration.html";
		}else {
			int otp=new Random().nextInt(100000,1000000);
			System.err.println(otp);
			vendor.setOtp(otp);
			vendor.setPassword(AES.encrypt(vendor.getPassword()));
			System.err.println(vendor);
			emailSender.send(vendor);
			session.setAttribute("success","Otp sent successfully");
			repository.save(vendor);
			return "redirect:/vendor/otp/"+vendor.getId();
		}
	}
	
	public String verifyOtp(int id,int otp,HttpSession session) {
		Vendor vendor = repository.findById(id).orElseThrow();
		if(vendor.getOtp()==otp) {
			vendor.setVerified(true);
			repository.save(vendor);
	     	session.setAttribute("success","Vendor account created successfully");
			return "redirect:/";
		}else {
			session.setAttribute("failure","Vendor account failed to create, OTP missmatch");
			return "redirect:/vendor/otp/"+vendor.getId();
		}
		
	}

	public String vendorLogin(String email, String password, HttpSession session) {
		
		return null;
	}
}
