package com.jsp.ekart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.ekart.dto.Order;

public interface OrderRepository extends JpaRepository<Order, Integer> {

}
