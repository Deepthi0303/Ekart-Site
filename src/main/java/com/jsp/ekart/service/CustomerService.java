package com.jsp.ekart.service;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.stream.Collectors;

import org.json.JSONObject;
import com.razorpay.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestParam;

import com.jsp.ekart.dto.Item;
import com.jsp.ekart.dto.Cart;
import com.jsp.ekart.dto.Customer;
import com.jsp.ekart.dto.Product;
import com.jsp.ekart.dto.Vendor;
import com.jsp.ekart.helper.AES;
import com.jsp.ekart.helper.EmailSender;
import com.jsp.ekart.repository.CustomerRepository;
import com.jsp.ekart.repository.OrderRepository;
import com.jsp.ekart.repository.ProductRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Component
public class CustomerService {
	
	@Autowired
	CustomerRepository repository3;
	
	@Autowired
	ProductRepository repository4;
	
	@Autowired
	EmailSender emailSender;
	
	@Autowired
	OrderRepository orderRepository;
	
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
						session.setAttribute("customer", customer);
						System.err.println(session.getAttribute("customer"));
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

	public String viewProduct(HttpSession session, ModelMap map) {
		System.out.println(session.getAttribute("customer"));
		if (session.getAttribute("customer") != null) {
			List<Product> products = repository4.findByApprovedTrue();
			if (products.isEmpty()) {
				session.setAttribute("failure", "No Products Present");
				return "redirect:/customer/home";
			} else {
				map.put("products", products);
				return "customer-view-products.html";
			}
		} else {
			session.setAttribute("failure", "Invalid Session, First Login");
			return "redirect:/customer/login";
		}
	}

	public String searchProducts(HttpSession session) {
		if (session.getAttribute("customer") != null) {
			return "search.html";
		} else {
			session.setAttribute("failure", "Invalid Session, First Login");
			return "redirect:/customer/login";
		}
		
	}

	public String search(String query, HttpSession session, ModelMap map) {
		if (session.getAttribute("customer") != null) {
			String toSearch = "%" + query + "%";
			List<Product> list1 = repository4.findByNameLike(toSearch);
			List<Product> list2 = repository4.findByDescriptionLike(toSearch);
			List<Product> list3 = repository4.findByCategoryLike(toSearch);
		    HashSet<Product> products = new HashSet<Product>();
		    products.addAll(list1);
			products.addAll(list2);
			products.addAll(list3);
			map.put("products", products);
			map.put("query", query);
			return "search.html";
		} else {
			session.setAttribute("failure", "Invalid Session, First Login");
			return "redirect:/customer/login";
		}
	}

	public String viewCart(HttpSession session, ModelMap map) {
		if (session.getAttribute("customer") != null) {
			Customer customer = (Customer) session.getAttribute("customer");
			 Cart cart = customer.getCart();
			if (cart == null) {
				session.setAttribute("failure", "Nothing is Present inside Cart");
				return "redirect:/customer/home";
			} else {
				List<Item> items = cart.getItems();
				if (items.isEmpty()) {
					session.setAttribute("failure", "Nothing is Present inside Cart");
					return "redirect:/customer/home";
				} else {
					map.put("items", items);
					return "view-cart.html";
				}
			}
		} else {
			session.setAttribute("failure", "Invalid Session, First Login");
			return "redirect:/customer/login";
		}
	}

	public String addToCart(int id, HttpSession session) {
		System.err.println(session.getAttribute("customer"));
		if (session.getAttribute("customer") != null) {
			Product product = repository4.findById(id).get();
			if (product.getStock() > 0) {
				Customer customer = (Customer) session.getAttribute("customer");
				Cart cart = customer.getCart();
				 if (cart == null) {
		                cart = new Cart();
		                customer.setCart(cart); // Set the initialized cart back to the customer
		            }
				 
				List<Item> items = cart.getItems();
				if (items == null) {
	                items = new ArrayList<>();
	                cart.setItems(items); // Set the initialized list back to the cart
	            }
				if (items.stream().map(x -> x.getName()).collect(Collectors.toList()).contains(product.getName())) {
					session.setAttribute("failure", "Product Already Exists in Cart");
					return "redirect:/customer/home";
				} else {
					Item item = new Item();
					item.setName(product.getName());
					item.setCategory(product.getCategory());
					item.setDescription(product.getDescription());
					item.setImageLink(product.getImageLink());
					item.setPrice(product.getPrice());
					item.setQuantity(1);
					items.add(item);
					
				    repository3.save(customer);
					session.setAttribute("success", "Product Added to Cart Success");
					session.setAttribute("customer", repository3.findById(customer.getId()).get());
					return "redirect:/customer/home";
				}
			} else {
				session.setAttribute("failure", "Sorry! Product Out of Stock");
				return "redirect:/customer/home";
			}
		} else {
			session.setAttribute("failure", "Invalid Session, First Login");
			return "redirect:/customer/login";
		}
	}

	public String payment(HttpSession session, ModelMap map) {
			Customer customer = (Customer) session.getAttribute("customer");
			if (session.getAttribute("customer") != null) {
				try {
					double amount = customer.getCart().getItems().stream().mapToDouble(i -> i.getPrice()).sum();
					RazorpayClient client = new RazorpayClient("rzp_test_uoiVA3aDuFUZca", "OHIVfcji4kqhgPDuiFlxMVcy");
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("currency", "INR");
					jsonObject.put("amount", amount * 100);
					Order order = client.orders.create(jsonObject);
					map.put("key", "rzp_test_uoiVA3aDuFUZca");
					map.put("id", order.get("id"));
					map.put("amount", amount * 100);
					map.put("customer", customer);
					return "payment.html";
				} catch (RazorpayException e) {
					session.setAttribute("failure", "Invalid Session, First Login");
					return "redirect:/customer/login";
				}
			} else {
				session.setAttribute("failure", "Invalid Session, First Login");
				return "redirect:/customer/login";
			}
		}

	public String paymentSuccess(com.jsp.ekart.dto.Order order, HttpSession session) {
		if (session.getAttribute("customer") != null) {
			Customer customer = (Customer) session.getAttribute("customer");
			order.setCustomer(customer);
			order.setTotalPrice(customer.getCart().getItems().stream().mapToDouble(i -> i.getPrice()).sum());
			List<Item> items = customer.getCart().getItems();
			System.out.println(items.size());
			
//			List<Item> orderItems=order.getItems();
//			for(Item item:items) {
//				orderItems.add(item);
//			}
			
//			First this was there above one( it was not updating in database,error was showing)
			
			List<Item> orderItems = order.getItems();
			for (Item item : items) {
				Item item2 = new Item();
				item2.setCategory(item.getCategory());
				item2.setDescription(item.getDescription());
				item2.setImageLink(item.getImageLink());
				item2.setName(item.getName());
				item2.setPrice(item.getPrice());
				item2.setQuantity(item.getQuantity());
				orderItems.add(item2);
			}
			order.setItems(orderItems);
			orderRepository.save(order);
			
			
			customer.getCart().getItems().clear();
			repository3.save(customer);
			
			session.setAttribute("customer", repository3.findById(customer.getId()).get());
			session.setAttribute("success", "Order Placed Success");
			return "redirect:/customer/home";
		} else {
			session.setAttribute("failure", "Invalid Session, First Login");
			return "redirect:/customer/login";
		}
	}

	public String viewOrders(HttpSession session, ModelMap map) {
		if (session.getAttribute("customer") != null) {
			Customer customer = (Customer) session.getAttribute("customer");
			List<com.jsp.ekart.dto.Order> orders = orderRepository.findByCustomer(customer);
			if (orders.isEmpty()) {
				session.setAttribute("success", "No Orders Placed Yet");
				return "redirect:/customer/home";
			} else {
				map.put("orders", orders);
				return "view-orders.html";
			}
		} else {
			session.setAttribute("failure", "Invalid Session, First Login");
			return "redirect:/customer/login";
		}
		
	}
	}
	
