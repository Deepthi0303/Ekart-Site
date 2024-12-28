package com.jsp.ekart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.jsp.ekart.dto.Product;

@Component
public interface ProductRepository extends JpaRepository<Product, Integer>{

}
