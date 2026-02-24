package com.bootcamp.ecommerce.controller;

import com.bootcamp.ecommerce.DTO.CartRequest;
import com.bootcamp.ecommerce.DTO.CartResponseDTO;
import com.bootcamp.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/add/product/cart")
    public ResponseEntity<String> addToCart(@Valid @RequestBody CartRequest request) {
        cartService.addToCart(request);
        return ResponseEntity.ok("Product added to cart successfully");
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/view/cart")
    public ResponseEntity<List<CartResponseDTO>> viewCart() {
        return ResponseEntity.ok(cartService.viewCart());
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/delete/{productVariationId}")
    public ResponseEntity<String> removeFromCart(@PathVariable Long productVariationId) {
        cartService.removeFromCart(productVariationId);
        return ResponseEntity.ok("Product removed from cart");
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/update/cart")
    public ResponseEntity<String> updateCart(@Valid @RequestBody CartRequest request) {
        cartService.updateCart(request);
        return ResponseEntity.ok("Cart updated successfully");
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/delete/cart")
    public ResponseEntity<?> emptyCart() {
        cartService.emptyCart();
        return ResponseEntity.ok("Cart emptied successfully");
    }

}