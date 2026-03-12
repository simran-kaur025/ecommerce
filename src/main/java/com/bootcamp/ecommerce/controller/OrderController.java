package com.bootcamp.ecommerce.controller;

import com.bootcamp.ecommerce.DTO.*;
import com.bootcamp.ecommerce.service.OrderService;
import com.bootcamp.ecommerce.utils.RequestParamsExtractor;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;

@RequestMapping("/api/order")
@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final RequestParamsExtractor extractor;

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/place/order")
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest placeOrderRequest, Locale locale) {
        OrderResponse response = orderService.placeOrderForCurrentUser(placeOrderRequest, locale);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/place/partial/order")
    public ResponseEntity<?> placePartialOrder(@Valid @RequestBody PartialOrderRequest request) {

        OrderResponse response = orderService.placePartialOrder(request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/place/direct/order")
    public ResponseEntity<OrderResponse> directOrder(@Valid @RequestBody OrderRequest request) {

        OrderResponse orderResponse = orderService.directOrder(request);
        return ResponseEntity.ok(orderResponse);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PatchMapping("/cancel/{orderProductId}")
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderProductId) {

        orderService.cancelOrder(orderProductId);
        return ResponseEntity.ok("Order cancelled successfully");
    }


    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/orders/return/{orderProductId}")
    public ResponseDTO returnOrder(@PathVariable Long orderProductId) {
        orderService.returnOrder(orderProductId);
        return new ResponseDTO("SUCCESS", "Return request placed", null);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/view/{orderId}")
    public ResponseEntity<OrderResponseDTO> viewMyOrder(@PathVariable Long orderId) {
        OrderResponseDTO response = orderService.viewMyOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/view/all/orders")
    public ResponseEntity<Page<OrderResponseDTO>> listMyOrders(@RequestParam Map<String ,String> allParams) {
        RequestParams requestParams = extractor.extract(allParams);
        Page<OrderResponseDTO> response = orderService.listMyOrders(requestParams);

        return ResponseEntity.ok(response);
    }



/* Seller Order Api */
    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/seller/view/all/orders")
    public ResponseEntity<Page<OrderResponseDTO>> listOrdersOfMyProducts(@RequestParam Map<String ,String> allParams) {
        RequestParams requestParams = extractor.extract(allParams);

        Page<OrderResponseDTO> response = orderService.listOrdersOfMyProducts(requestParams);

        return ResponseEntity.ok(response);
    }



    // Admin Order Api
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/get/all/orders")
    public ResponseEntity<Page<OrderResponseDTO>>  getAllOrders(@RequestParam Map<String ,String> allParams) {
        RequestParams requestParams = extractor.extract(allParams);
        Page<OrderResponseDTO> response = orderService.getAllOrdersAsAdmin(requestParams);
        return ResponseEntity.ok(response);
    }
}
