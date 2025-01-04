package com.jsp.ekart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.jsp.ekart.dto.Product;
import com.jsp.ekart.dto.Vendor;

@Component
public interface ProductRepository extends JpaRepository<Product, Integer>{

	List<Product> findByVendor(Vendor vendor);

	List<Product> findByApprovedTrue();

	List<Product> findByNameLike(String toSearch);

	List<Product> findByDescriptionLike(String toSearch);

	List<Product> findByCategoryLike(String toSearch);

}
