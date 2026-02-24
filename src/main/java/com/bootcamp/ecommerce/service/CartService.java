package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.DTO.CartRequest;
import com.bootcamp.ecommerce.DTO.CartResponseDTO;

import java.util.List;

public interface CartService {
    void addToCart(CartRequest request);
    List<CartResponseDTO> viewCart();
    void removeFromCart(Long productVariationId);
    void updateCart(CartRequest request);
    void emptyCart();
}
