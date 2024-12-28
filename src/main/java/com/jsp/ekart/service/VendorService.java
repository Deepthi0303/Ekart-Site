package com.jsp.ekart.service;

import java.io.IOException;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;

import com.jsp.ekart.dto.Product;
import com.jsp.ekart.dto.Vendor;
import com.jsp.ekart.helper.AES;
import com.jsp.ekart.helper.CloudinaryHelper;
import com.jsp.ekart.helper.EmailSender;
import com.jsp.ekart.repository.ProductRepository;
import com.jsp.ekart.repository.VendorRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Service
public class VendorService {
	
	@Autowired
	VendorRepository repository;
	
	@Autowired
	EmailSender emailSender;
	
	@Autowired
	ProductRepository repository2;
	
	@Autowired
	CloudinaryHelper cloudinaryHelper;
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
		
		Vendor vendor = repository.findByEmail(email);
		if(vendor==null) {
			session.setAttribute("vendor", vendor);
			session.setAttribute("failure","Email is not registered");
			return "redirect:/vendor/login";
		}
		else {
			if(AES.decrypt(vendor.getPassword()).equals(password)) {
				if(vendor.isVerified()) {
					session.setAttribute("vendor", vendor);
					session.setAttribute("success", "Account logged in");
					return "redirect:/vendor/home";
				}else {
					int otp=new Random().nextInt(100000,1000000);
					vendor.setOtp(otp);
					emailSender.send(vendor);
					repository.save(vendor);
					System.err.println(vendor.getOtp());
					session.setAttribute("success","OTP sent successfully, Please verify now");
					return "redirect:/vendor/otp/"+vendor.getId();
				}
			}
			  else {
					session.setAttribute("failure", "Invalid Password");
					return "redirect:/vendor/login";
				}
			}
		}

	public String loadAddProduct(HttpSession session) {
		if(session.getAttribute("vendor")!=null)
		{
			return "add-product.html";
		}else {
			session.setAttribute("failure", "Invalid Session, First Login");
			return "redirect:/vendor/login";
		}
	}

	public String addProduct(Product product,HttpSession session) throws IOException {
		if(session.getAttribute("vendor")!=null)
		{
			Vendor vendor = (Vendor) session.getAttribute("vendor");
			product.setVendor(vendor);
			product.setImageLink(cloudinaryHelper.saveToCloudinary(product.getImage()));
			repository2.save(product);
			session.setAttribute("success", "Product Added Success");
			return "redirect:/vendor/home";
		}else {
			session.setAttribute("failure", "Invalid Session, First Login");
			return "redirect:/vendor/login";
		}
	}

	public String logout(HttpSession session) {
		session.removeAttribute("vendor");
		session.setAttribute("success", "Logged out Success");
		return "redirect:/";
	}
	
	
	}

