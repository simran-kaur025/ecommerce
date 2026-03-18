package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.DTO.CartRequest;
import com.bootcamp.ecommerce.DTO.CartResponseDTO;
import com.bootcamp.ecommerce.entity.Cart;
import com.bootcamp.ecommerce.entity.CartId;
import com.bootcamp.ecommerce.entity.ProductVariation;
import com.bootcamp.ecommerce.entity.User;
import com.bootcamp.ecommerce.exceptionalHandler.BadRequestException;
import com.bootcamp.ecommerce.exceptionalHandler.InsufficientStockException;
import com.bootcamp.ecommerce.exceptionalHandler.ProductInactiveException;
import com.bootcamp.ecommerce.exceptionalHandler.ResourceNotFoundException;
import com.bootcamp.ecommerce.repository.CartRepository;
import com.bootcamp.ecommerce.repository.ProductVariationRepository;
import com.bootcamp.ecommerce.repository.UserRepository;
import com.bootcamp.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductVariationRepository variationRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public void addToCart(CartRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Add to cart request received from user: {}", email);


        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ProductVariation variation = variationRepository.findById(request.getProductVariationId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid product variation"));

        if (!variation.getIsActive())
            throw new ProductInactiveException("Product variation not active");

        if (variation.getProduct().getIsDeleted())
            throw new ResourceNotFoundException("Product deleted");

        if (request.getQuantity() <= 0)
            throw new BadRequestException("Quantity must be greater than 0");

        if (variation.getQuantity_available() < request.getQuantity())
            throw new InsufficientStockException("Insufficient stock");

        Optional<Cart> existing = cartRepository.findByCustomerAndProductVariation(user, variation);

        if (existing.isPresent()) {
            Cart cart = existing.get();
            int oldQuantity = cart.getQuantity();
            cart.setQuantity(cart.getQuantity() + request.getQuantity());
            cartRepository.save(cart);
            log.info("Updated cart for user {}: ProductVariation {} quantity {} -> {}", user.getEmail(), variation.getId(), oldQuantity, cart.getQuantity());
        } else {
            CartId cartId = new CartId(user.getId(), variation.getId());

            Cart cart = new Cart();
            cart.setId(cartId);
            cart.setCustomer(user);
            cart.setProductVariation(variation);
            cart.setQuantity(request.getQuantity());
            cart.setIsWishlistItem(false);

            cartRepository.save(cart);
            log.info("Added new cart item for user {}: ProductVariation {} quantity {}", user.getEmail(), variation.getId(), cart.getQuantity());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<CartResponseDTO> viewCart() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("View cart request received from user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Cart> cartItems = cartRepository.findByCustomerAndIsWishlistItemFalse(user);
        log.info("User {} has {} items in the cart", email, cartItems.size());

        return cartItems.stream()
                .map(cart -> {

                    ProductVariation variation = cart.getProductVariation();

                    boolean outOfStock = variation.getQuantity_available() < cart.getQuantity();

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

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Remove from cart request received from user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ProductVariation variation = variationRepository.findById(productVariationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid product variation"));

        Cart cart = cartRepository.findByCustomerAndProductVariationAndIsWishlistItemFalse(user, variation)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in cart"));

        cartRepository.delete(cart);
        log.info("Removed ProductVariation {} from cart for user {}", variation.getId(), email);

    }


    @Transactional
    @Override
    public void updateCart(CartRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Update cart request received from user: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ProductVariation variation = variationRepository.findById(request.getProductVariationId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid product variation"));

        if (!variation.getIsActive())
            throw new ProductInactiveException("Variation not active");

        if (variation.getProduct().getIsDeleted())
            throw new ResourceNotFoundException("Product deleted");

        Cart cart = cartRepository.findByCustomerAndProductVariationAndIsWishlistItemFalse(user, variation)
                .orElseThrow(() -> new ResourceNotFoundException("Product not in cart"));

        if (request.getQuantity() == 0) {
            cartRepository.delete(cart);
            return;
        }

        if (variation.getQuantity_available() < request.getQuantity())
            throw new InsufficientStockException("Insufficient stock");

        int oldQuantity = cart.getQuantity();
        cart.setQuantity(request.getQuantity());
        cartRepository.save(cart);
        log.info("Updated cart for user {}: ProductVariation {} quantity {} -> {}", email, variation.getId(), oldQuantity, cart.getQuantity());
    }



    @Transactional
    @Override
    public void emptyCart() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Empty cart request received from user: {}", email);


        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Cart> cartItems = cartRepository.findByCustomerAndIsWishlistItemFalse(user);

        int count = cartItems.size();
        cartRepository.deleteAll(cartItems);
        log.info("Emptied cart for user {}. Total items removed: {}", email, count);
    }
}
