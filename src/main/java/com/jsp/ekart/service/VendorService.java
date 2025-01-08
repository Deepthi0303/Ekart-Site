package com.jsp.ekart.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.jsp.ekart.dto.Customer;
import com.jsp.ekart.dto.Item;
import com.jsp.ekart.dto.Product;
import com.jsp.ekart.dto.Vendor;
import com.jsp.ekart.helper.AES;
import com.jsp.ekart.helper.CloudinaryHelper;
import com.jsp.ekart.helper.EmailSender;
import com.jsp.ekart.repository.CustomerRepository;
import com.jsp.ekart.repository.ItemRepository;
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
	CustomerRepository customerRepository;

	@Autowired
	ItemRepository itemRepository;

	@Autowired
	CloudinaryHelper cloudinaryHelper;

	public String loadRegistration(ModelMap map, Vendor vendor) {
		map.put("vendor", vendor);
		return "vendor-registration.html";
	}

	public String vendorRegistration(Vendor vendor, BindingResult result, HttpSession session) {
		if (!vendor.getPassword().equals(vendor.getConfirmPassword())) {
			result.rejectValue("confirmPassword", "error.confirmPassword",
					"* Password and Confirm Password Should Match");
		}
		if (repository.existsByEmail(vendor.getEmail())) {
			result.rejectValue("email", "error.email", "* Email already exist");
		}
		if (repository.existsByMobile(vendor.getMobile()))
			result.rejectValue("mobile", "error.mobile", "* Mobile Number Already Exists");
		if (result.hasErrors()) {
			return "vendor-registration.html";
		} else {
			int otp = new Random().nextInt(100000, 1000000);
			System.err.println(otp);
			vendor.setOtp(otp);
			vendor.setPassword(AES.encrypt(vendor.getPassword()));
			System.err.println(vendor);
			emailSender.send(vendor);
			session.setAttribute("success", "Otp sent successfully");
			repository.save(vendor);
			return "redirect:/vendor/otp/" + vendor.getId();
		}
	}

	public String verifyOtp(int id, int otp, HttpSession session) {
		Vendor vendor = repository.findById(id).orElseThrow();
		if (vendor.getOtp() == otp) {
			vendor.setVerified(true);
			repository.save(vendor);
			session.setAttribute("success", "Vendor account created successfully");
			return "redirect:/";
		} else {
			session.setAttribute("failure", "Vendor account failed to create, OTP missmatch");
			return "redirect:/vendor/otp/" + vendor.getId();
		}

	}

	public String vendorLogin(String email, String password, HttpSession session) {

		Vendor vendor = repository.findByEmail(email);
		if (vendor == null) {
			session.setAttribute("vendor", vendor);
			session.setAttribute("failure", "Email is not registered");
			return "redirect:/vendor/login";
		} else {
			if (AES.decrypt(vendor.getPassword()).equals(password)) {
				if (vendor.isVerified()) {
					session.setAttribute("vendor", vendor);
					session.setAttribute("success", "Account logged in");
					return "redirect:/vendor/home";
				} else {
					int otp = new Random().nextInt(100000, 1000000);
					vendor.setOtp(otp);
					emailSender.send(vendor);
					repository.save(vendor);
					System.err.println(vendor.getOtp());
					session.setAttribute("success", "OTP sent successfully, Please verify now");
					return "redirect:/vendor/otp/" + vendor.getId();
				}
			} else {
				session.setAttribute("failure", "Invalid Password");
				return "redirect:/vendor/login";
			}
		}
	}

	public String loadAddProduct(HttpSession session) {
		if (session.getAttribute("vendor") != null) {
			return "add-product.html";
		} else {
			session.setAttribute("failure", "Invalid Session, First Login");
			return "redirect:/vendor/login";
		}
	}

	public String addProduct(Product product, HttpSession session) throws IOException {
		if (session.getAttribute("vendor") != null) {
			Vendor vendor = (Vendor) session.getAttribute("vendor");
			product.setVendor(vendor);
			product.setImageLink(cloudinaryHelper.saveToCloudinary(product.getImage()));
			repository2.save(product);
			session.setAttribute("success", "Product Added Success");
			return "redirect:/vendor/home";
		} else {
			session.setAttribute("failure", "Invalid Session, First Login");
			return "redirect:/vendor/login";
		}
	}

	public String logout(HttpSession session) {
		session.removeAttribute("vendor");
		session.setAttribute("success", "Logged out Success");
		return "redirect:/";
	}

	public String manageProducts(HttpSession session, ModelMap map) {
		Vendor vendor = (Vendor) session.getAttribute("vendor");
		if (vendor != null) {
			List<Product> products = repository2.findByVendor(vendor);
			if (products.isEmpty()) {
				session.setAttribute("failure", "No products added");
				return "redirect:/vendor/home";
			} else {
				session.setAttribute("success", "Products added are");
				map.put("products", products);
				return "manage-product.html";
			}
		} else {
			session.setAttribute("failure", "Invalid Session, Login Again");
			return "redirect:/vendor/login";
		}
	}

	public String delete(int id, HttpSession session) {
		if (session.getAttribute("vendor") != null) {
			repository2.deleteById(id);
			session.setAttribute("success", "Product Deleted Success");
			return "redirect:/manage-product";
		} else {
			session.setAttribute("failure", "Invalid Session, First Login");
			return "redirect:/vendor/login";
		}
	}

	public String editProduct(int id, ModelMap map, HttpSession session) {
		if (session.getAttribute("vendor") != null) {
			Optional<Product> pro = repository2.findById(id);
			Product product = pro.get();
			map.put("products", product);
			return "edit-products.html";
		} else {
			session.setAttribute("failure", "Session not found,Please Login First");
			return "redirect:/vendor/login";
		}
	}

	public String updateProduct(int id, Product product, HttpSession session) throws IOException {
		if (session.getAttribute("vendor") != null) {
			Vendor vendor = (Vendor) session.getAttribute("vendor");
			System.out.println(vendor);
			Product existpro = repository2.findById(id).get();
			if (product.getImage() != null && !product.getImage().isEmpty()) {
				product.setImageLink(cloudinaryHelper.saveToCloudinary(product.getImage()));
			} else {
				product.setImageLink(existpro.getImageLink());
			}
			product.setVendor(vendor);
			repository2.save(product);
			session.setAttribute("success", "Product Updated Success");
			return "redirect:/manage-product";
		} else {
			session.setAttribute("failure", "Session not found,Please Login First");
			return "redirect:/vendor/login";
		}
	}

	public String increaseQuantity(int id, HttpSession session) {
		if (session.getAttribute("customer") != null) {
			Customer customer = (Customer) session.getAttribute("customer");
			Item item = itemRepository.findById(id).get();
			Product product = repository2.findByNameLike(item.getName()).get(0);
			if (product.getStock() == 0) {
				session.setAttribute("failure", "Sorry! Product Out of Stock");
				return "redirect:/view-cart";
			} else {
				item.setQuantity(item.getQuantity() + 1);
				item.setPrice(item.getPrice() + product.getPrice());
				itemRepository.save(item);
				product.setStock(product.getStock() - 1);
				repository2.save(product);
				session.setAttribute("success", "Product Added to Cart Success");
				session.setAttribute("customer", customerRepository.findById(customer.getId()).get());
				return "redirect:/view-cart";
			}
		} else {
			session.setAttribute("failure", "Invalid Session, First Login");
			return "redirect:/customer/login";
		}
	}

	public String decreaseQuantity(int id, HttpSession session) {
		if (session.getAttribute("customer") != null) {
			Customer customer = (Customer) session.getAttribute("customer");
			Item item = itemRepository.findById(id).get();
			Product product = repository2.findByNameLike(item.getName()).get(0);
			
			if(item.getQuantity()>1) {
				item.setQuantity(item.getQuantity()-1);
				item.setPrice(item.getPrice()-product.getPrice());
				itemRepository.save(item);
				product.setStock(product.getStock() + 1);
				repository2.save(product);
				session.setAttribute("success", "Product Removed from Cart Success");
				session.setAttribute("customer", customerRepository.findById(customer.getId()).get());
				return "redirect:/view-cart";
			}
			else {
				customer.getCart().getItems().remove(item);
				customerRepository.save(customer);
				session.setAttribute("success", "Product Quantity Reduced from Cart Success");
				session.setAttribute("customer", customerRepository.findById(customer.getId()).get());
				return "redirect:/view-cart";
			}
		} else {
			session.setAttribute("failure", "Invalid Session, First Login");
			return "redirect:/customer/login";
		}
	}
	}

