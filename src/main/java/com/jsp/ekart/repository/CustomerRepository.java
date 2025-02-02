package com.jsp.ekart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.ekart.dto.Customer;
import com.jsp.ekart.dto.Product;

public interface CustomerRepository extends JpaRepository<Customer, Integer>{

	boolean existsByEmail(String email);

	boolean existsByMobile(long mobile);

    Customer findByEmail(String email);

}
