package com.jsp.ekart.controller;

import org.aspectj.apache.bcel.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jsp.ekart.dto.Vendor;
import com.jsp.ekart.service.VendorService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class EkartController {
	
	@Autowired
	VendorService service;
	
	@GetMapping("/")
	public String loadHomePage() {
		return "home.html";
	}
	
	@GetMapping("/vender/register")
	public String loadVenderRegistration(ModelMap map,Vendor vendor) {
		return service.loadRegistration(map, vendor);
	}
	
	@PostMapping("/vendor/register")
	public String vendorRegistration(@Valid Vendor vendor,BindingResult result,HttpSession session) {
		return service.vendorRegistration(vendor, result,session);
	}
	
	@GetMapping("/vendor/otp/{id}")
	public String loadOtpPage(@PathVariable int id,ModelMap map) {
		map.put("id", id);
		return "vendor-otp.html";
	}
	@PostMapping("/vendor/otp")
	public String verifyOtp(@RequestParam int id,@RequestParam int otp,HttpSession session) {
		return service.verifyOtp(id,otp,session);
	}
	
	@GetMapping("/vender/login")
	public String loadVenderLogin() {
		return "vendor-login.html";
	}
	
	@PostMapping("/vender/login")
	public String VenderLogin(@RequestParam String email,@RequestParam String password,HttpSession session) {
		return service.vendorLogin(email,password,session);
	}
}