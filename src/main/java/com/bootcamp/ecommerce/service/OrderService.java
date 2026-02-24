package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.DTO.*;
import org.springframework.data.domain.Page;

public interface OrderService {
    OrderResponse placeOrderForCurrentUser(PlaceOrderRequest request);
    OrderResponse placePartialOrder(PartialOrderRequest request);
    OrderResponse directOrder(OrderRequest request);
    void cancelOrder(Long orderProductId);

    void returnOrder(Long orderProductId);
    OrderResponseDTO viewMyOrder(Long orderId);
    Page<OrderResponseDTO> listMyOrders(int max, int offset, String sort, String order, String query);
    Page<OrderResponseDTO> listOrdersOfMyProducts(int max, int offset, String sort, String order, String query) ;

    Page<OrderResponseDTO> getAllOrdersAsAdmin(int max, int offset,
                                               String sort, String order,
                                               String query);


}
