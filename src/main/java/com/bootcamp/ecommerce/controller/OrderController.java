package com.bootcamp.ecommerce.controller;

import com.bootcamp.ecommerce.DTO.*;
import com.bootcamp.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/order")
@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/place/order")
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest placeOrderRequest) {
        OrderResponse response = orderService.placeOrderForCurrentUser(placeOrderRequest);
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
    public ResponseEntity<Page<OrderResponseDTO>> listMyOrders(
            @RequestParam(defaultValue = "10") int max,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "dateCreated") String sort,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(required = false) String query) {

        Page<OrderResponseDTO> response = orderService.listMyOrders(max, offset, sort, order, query);

        return ResponseEntity.ok(response);
    }

/* Seller Order Api */
    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/seller/view/all/orders")
    public ResponseEntity<Page<OrderResponseDTO>> listOrdersOfMyProducts(
            @RequestParam(defaultValue = "10") int max,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "dateCreated") String sort,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(required = false) String query) {

        Page<OrderResponseDTO> response = orderService.listOrdersOfMyProducts(max, offset, sort, order, query);

        return ResponseEntity.ok(response);
    }



    // Admin Order Api
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/get/all/orders")
    public Page<OrderResponseDTO> getAllOrders(
            @RequestParam(defaultValue = "10") int max,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(required = false) String query
    ) {
        return orderService.getAllOrdersAsAdmin(max, offset, sort, order, query);
    }
}
