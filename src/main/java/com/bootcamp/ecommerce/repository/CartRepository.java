package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.Address;
import com.bootcamp.ecommerce.entity.Cart;
import com.bootcamp.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByCustomerAndIsWishlistItemFalse(User customer);


}
