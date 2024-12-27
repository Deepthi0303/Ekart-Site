package com.jsp.ekart.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
public class Vendor {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Size(min=5,max=30,message="* Enter between 5-30 characters")
	private String name;
	
	@DecimalMin(value="6000000000",message="* Enter Proper Mobile Number")
	@DecimalMax(value="9999999999",message="* Enter Proper Mobile Number")
	private long mobile;
	
	@Email(message="* Enter proper Email")
	@NotEmpty(message="* It is required field")
	private String email;
	
	@Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",message="* Minimum eight characters, at least one uppercase letter, one lowercase letter, one number and one special character")
	private String password;
	
	@Transient
	@Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",message="* Minimum eight characters, at least one uppercase letter, one lowercase letter, one number and one special character")
	private String confirmPassword;
	
	private int otp;
	private boolean verified;
	
}
