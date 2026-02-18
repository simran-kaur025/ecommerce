package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.DTO.OrderResponse;
import com.bootcamp.ecommerce.entity.*;
import com.bootcamp.ecommerce.enums.OrderState;
import com.bootcamp.ecommerce.repository.*;
import com.bootcamp.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderStatusRepository orderStatusRepository;

    @Transactional
    @Override
    public OrderResponse placeOrderForCurrentUser() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Cart> cartItems = cartRepository.findByCustomerAndIsWishlistItemFalse(user);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        for (Cart item : cartItems) {
            ProductVariation variation = item.getProductVariation();

            if (!variation.getIsActive()) {
                throw new RuntimeException("Invalid product variation");
            }

            if (variation.getQuantity_available() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock");
            }
        }

        Order order = new Order();
        order.setCustomer(user);

        orderRepository.save(order);

        for (Cart item : cartItems) {

            ProductVariation variation = item.getProductVariation();

            OrderProduct orderItem = new OrderProduct();
            orderItem.setOrder(order);
            orderItem.setProductVariation(variation);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(variation.getPrice());

            orderProductRepository.save(orderItem);

            OrderStatus status = new OrderStatus();
            status.setOrderProduct(orderItem);
            status.setFromStatus(null);
            status.setToStatus(OrderState.ORDER_PLACED);
            status.setUpdatedAt(LocalDateTime.now());

            orderStatusRepository.save(status);

            variation.setQuantity_available(variation.getQuantity_available() - item.getQuantity());
        }

        cartRepository.deleteAll(cartItems);

        return new OrderResponse("Order placed successfully", order.getId());
    }
}
