package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.DTO.CartRequest;
import com.bootcamp.ecommerce.DTO.CartResponseDTO;
import com.bootcamp.ecommerce.entity.Cart;
import com.bootcamp.ecommerce.entity.CartId;
import com.bootcamp.ecommerce.entity.ProductVariation;
import com.bootcamp.ecommerce.entity.User;
import com.bootcamp.ecommerce.repository.CartRepository;
import com.bootcamp.ecommerce.repository.ProductVariationRepository;
import com.bootcamp.ecommerce.repository.UserRepository;
import com.bootcamp.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductVariationRepository variationRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public void addToCart(CartRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProductVariation variation = variationRepository.findById(request.getProductVariationId())
                .orElseThrow(() -> new RuntimeException("Invalid product variation"));

        if (!variation.getIsActive())
            throw new RuntimeException("Product variation not active");

        if (variation.getProduct().getIsDeleted())
            throw new RuntimeException("Product deleted");

        if (request.getQuantity() <= 0)
            throw new RuntimeException("Quantity must be greater than 0");

        if (variation.getQuantity_available() < request.getQuantity())
            throw new RuntimeException("Insufficient stock");

        Optional<Cart> existing = cartRepository.findByCustomerAndProductVariation(user, variation);

        if (existing.isPresent()) {
            Cart cart = existing.get();
            cart.setQuantity(cart.getQuantity() + request.getQuantity());
            cartRepository.save(cart);
        } else {
            CartId cartId = new CartId(user.getId(), variation.getId());

            Cart cart = new Cart();
            cart.setId(cartId);
            cart.setCustomer(user);
            cart.setProductVariation(variation);
            cart.setQuantity(request.getQuantity());
            cart.setIsWishlistItem(false);

            cartRepository.save(cart);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<CartResponseDTO> viewCart() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Cart> cartItems = cartRepository.findByCustomerAndIsWishlistItemFalse(user);

        return cartItems.stream()
                .map(cart -> {

                    ProductVariation variation = cart.getProductVariation();

                    boolean outOfStock =
                            variation.getQuantity_available() < cart.getQuantity();

                    return new CartResponseDTO(
                            variation.getId(),
                            variation.getProduct().getName(),
                            cart.getQuantity(),
                            variation.getPrice(),
                            outOfStock
                    );

                })
                .toList();
    }

    @Transactional
    @Override
    public void removeFromCart(Long productVariationId) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProductVariation variation = variationRepository.findById(productVariationId)
                .orElseThrow(() -> new RuntimeException("Invalid product variation"));

        Cart cart = cartRepository.findByCustomerAndProductVariationAndIsWishlistItemFalse(user, variation)
                .orElseThrow(() -> new RuntimeException("Product not found in cart"));

        cartRepository.delete(cart);
    }


    @Transactional
    @Override
    public void updateCart(CartRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProductVariation variation = variationRepository.findById(request.getProductVariationId())
                .orElseThrow(() -> new RuntimeException("Invalid product variation"));

        if (!variation.getIsActive())
            throw new RuntimeException("Variation not active");

        if (variation.getProduct().getIsDeleted())
            throw new RuntimeException("Product deleted");

        Cart cart = cartRepository.findByCustomerAndProductVariationAndIsWishlistItemFalse(user, variation)
                .orElseThrow(() -> new RuntimeException("Product not in cart"));

        if (request.getQuantity() == 0) {
            cartRepository.delete(cart);
            return;
        }

        if (variation.getQuantity_available() < request.getQuantity())
            throw new RuntimeException("Insufficient stock");

        cart.setQuantity(request.getQuantity());
        cartRepository.save(cart);
    }



    @Transactional
    @Override
    public void emptyCart() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Cart> cartItems =
                cartRepository.findByCustomerAndIsWishlistItemFalse(user);

        cartRepository.deleteAll(cartItems);
    }
}
