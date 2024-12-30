package com.jsp.ekart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jsp.ekart.dto.Product;
import com.jsp.ekart.service.AdminService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {
	
	@Autowired
	AdminService service;
	
	@GetMapping("/admin/login")
	public String loadAdminLogin() {
		return "admin-login.html";
	}
	
	@PostMapping("/admin/login")
	public String adminLogin(@RequestParam String email,@RequestParam String password,HttpSession session) {
		return service.adminLogin(email,password,session);
	}
	
	@Value("${admin.email}")
	String adminEmail;
	
	@GetMapping("/admin/home")
	public String loadAdminHome(HttpSession session) {
		System.out.println(session.getAttribute("admin"));
		if(session.getAttribute("admin")!=null) {
			System.out.println(session.getAttribute("admin")+"----");
			return "admin-home.html";	
		}else {
			session.setAttribute("failure","Please Login in");
			return "redirect:/admin/login";
		}
	}
	
	@GetMapping("/product/approve")
	public String productApprove(HttpSession session,ModelMap map) {
		session.setAttribute("admin", adminEmail);
		System.out.println(session.getAttribute("admin"));
		return service.productApprove(session,map);
	}
	
	@GetMapping("/change/{id}")
	public String changeStatus(@PathVariable int id, HttpSession session) {
		return service.changeStatus(id, session);
	}
}
