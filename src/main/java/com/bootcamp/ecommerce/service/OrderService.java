package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.DTO.*;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Locale;

public interface OrderService {
    OrderResponse placeOrderForCurrentUser(PlaceOrderRequest request, Locale locale);
    OrderResponse placePartialOrder(PartialOrderRequest request);
    OrderResponse directOrder(OrderRequest request);
    void cancelOrder(Long orderProductId);

    void returnOrder(Long orderProductId);
    OrderResponseDTO viewMyOrder(Long orderId);
    Page<OrderResponseDTO> listMyOrders(RequestParams params);

    Page<OrderResponseDTO> listOrdersOfMyProducts(RequestParams params) ;

    Page<OrderResponseDTO> getAllOrdersAsAdmin(RequestParams requestParams);

    void notifyPendingOrders();

    void updateOrderStatus(UpdateOrderStatusRequest request, UserDetails userDetails);





}
