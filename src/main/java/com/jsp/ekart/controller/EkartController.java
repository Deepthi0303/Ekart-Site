package com.jsp.ekart.controller;

import java.io.IOException;

import org.aspectj.apache.bcel.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jsp.ekart.dto.Product;
import com.jsp.ekart.dto.Vendor;
import com.jsp.ekart.service.VendorService;

import ch.qos.logback.core.model.Model;
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
	
	@GetMapping("/vendor/register")
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
	
	@GetMapping("/vendor/login")
	public String loadVenderLogin() {
		return "vendor-login.html";
	}
	
	@PostMapping("/vendor/login")
	public String VenderLogin(@RequestParam String email,@RequestParam String password,HttpSession session) {
		return service.vendorLogin(email,password,session);
	}
	
	@GetMapping("/vendor/home")
	public String loadVenderHome() {
		return "vendor-home.html";
	}
	
	@GetMapping("/add-products")
	public String loadAddProduct(HttpSession session) {
		return service.loadAddProduct(session);
	}
	
	@PostMapping("/add-products")
	public String addProduct(Product product,HttpSession session) throws IOException {
		return service.addProduct(product,session);
	}
	@GetMapping("/logout")
	public String logout(HttpSession session) {
		return service.logout(session);
	}

	@GetMapping("/manage-product")
	public String manageProducts(HttpSession session,ModelMap map) {
		return service.manageProducts(session,map);
	}
	@GetMapping("/delete/{id}")
	public String delete(@PathVariable int id,HttpSession session) {
		return service.delete(id,session);
	}
}
