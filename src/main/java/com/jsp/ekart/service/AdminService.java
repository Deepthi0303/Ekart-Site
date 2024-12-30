package com.jsp.ekart.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import com.jsp.ekart.dto.Product;
import com.jsp.ekart.repository.ProductRepository;

import jakarta.servlet.http.HttpSession;

@Component
public class AdminService {
	
	@Autowired
	ProductRepository repository2;
	
	
	@Value("${admin.email}")
	public String adminEmail;
	
	@Value("${admin.password}")
	public String adminPassword;
	
	public String adminLogin(String email,String password,HttpSession session) {
		if(adminEmail.equals(email))
		{
			if(adminPassword.equals(password))
			{
				session.setAttribute("admin", adminEmail);
				System.out.println(session.getAttribute("admin"));
				session.setAttribute("success","Admin Logged in");
				return "redirect:/admin/home";
			}else {
				session.setAttribute("failure","Invalid Password");
				return "redirect:/admin/login";
			}
		}else {
			session.setAttribute("failure","Email does not exist");
			return "redirect:/admin/login";
		}
	}

	public String productApprove(HttpSession session, ModelMap map) {
		System.out.println(session.getAttribute("admin"));
		if(session.getAttribute("admin")!=null)
		{
			List<Product> product = repository2.findAll();
			if(product.isEmpty())
			{
				session.setAttribute("failure","No products found");
				return "redirect:/admin/home";
			}else {
				session.setAttribute("admin",adminEmail);
				map.put("product", product);
				return "approve-product.html";
			}
		}else {
			session.setAttribute("failure","Session Expired please login");
			return "redirect:/admin/login";
		}
	
	}

	public String changeStatus(int id, HttpSession session) {
		session.setAttribute("admin", adminEmail);
		if (session.getAttribute("admin") != null) {
			Product product =repository2.findById(id).get();
			if (product.isApproved())
				product.setApproved(false);
			else
				product.setApproved(true);
			repository2.save(product);
			session.setAttribute("success", "Product Status Changed Success");
			return "redirect:/product/approve";
		} else {
			session.setAttribute("failure", "Invalid Session, First Login");
			return "redirect:/admin/login";
		}
	}
}
