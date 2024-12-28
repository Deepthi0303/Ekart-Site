package com.jsp.ekart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.jsp.ekart.dto.Vendor;

@Component
public interface VendorRepository extends JpaRepository<Vendor, Integer> {

	 boolean existsByEmail(String email);

	boolean existsByMobile(long mobile);
	
	Vendor findByEmail(String email);
}
