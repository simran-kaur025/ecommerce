package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.Address;
import com.bootcamp.ecommerce.entity.Cart;
import com.bootcamp.ecommerce.entity.ProductVariation;
import com.bootcamp.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCustomerAndProductVariation(User user, ProductVariation variation);
    List<Cart> findByCustomerAndIsWishlistItemFalse(User user);
    Optional<Cart> findByCustomerAndProductVariationAndIsWishlistItemFalse(User user, ProductVariation variation);

}
