package com.jsp.ekart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jsp.ekart.dto.Customer;
import com.jsp.ekart.service.CustomerService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class CustomerController {
	
	@Autowired
	CustomerService service;
	
	@GetMapping("/customer/register")
	public String loadCustomerRegistration(ModelMap map,Customer customer) {
		return service.loadCustomerRegistration(map, customer);
	}
	
	@PostMapping("/customer/register")
	public String customerRegistration(@Valid Customer customer,BindingResult result,HttpSession session) {
		return service.customerRegistration(customer,result,session);
	}
	
	@GetMapping("/customer/otp/{id}")
	public String loadOtpPage(@PathVariable int id,ModelMap map) {
		map.put("id", id);
		return "customer-otp.html";
	}
	@PostMapping("/customer/otp")
	public String verifyOtp(@RequestParam int id,@RequestParam int otp,HttpSession session) {
		return service.verifyOtp(id,otp,session);
	}
	
	@GetMapping("/customer/login")
	public String loadCustomerLogin() {
		return "customer-login.html";
	}
	
	@PostMapping("/customer/login")
	public String customerLogin(@RequestParam String email,@RequestParam String password,HttpSession session) {
		return service.customerLogin(email,password,session);
	}
	
	@GetMapping("/customer/home")
	public String loadCustomerHome() {
		return "customer-home.html";
	}
	
	@GetMapping("/view-products")
	public String viewProducts(HttpSession session, ModelMap map) {
		System.out.println(session.getAttribute("customer"));
		return service.viewProduct(session,map);
	}
	
	@GetMapping("/search-products")
	public String searchProducts(HttpSession session) {
		return service.searchProducts(session);
	}
	@PostMapping("/search-products")
	public String search(@RequestParam String query, HttpSession session, ModelMap map) {
		return service.search(query,session,map);
	}
}

