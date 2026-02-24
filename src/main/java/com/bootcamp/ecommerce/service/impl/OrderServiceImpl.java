package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.DTO.*;
import com.bootcamp.ecommerce.entity.*;
import com.bootcamp.ecommerce.enums.OrderState;
import com.bootcamp.ecommerce.enums.PaymentMethod;
import com.bootcamp.ecommerce.repository.*;
import com.bootcamp.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.bootcamp.ecommerce.enums.OrderState.RETURN_REQUESTED;
import static com.bootcamp.ecommerce.enums.PaymentMethod.COD;


@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final ProductVariationRepository productVariationRepository;
    private final AddressRepository addressRepository;


    @Transactional
    @Override
    public OrderResponse placeOrderForCurrentUser(PlaceOrderRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Address does not belong to user");
        }

        List<Cart> cartItems = cartRepository.findByCustomerAndIsWishlistItemFalse(user);

        if (cartItems.isEmpty())
            throw new RuntimeException("Cart is empty");

        double totalAmount = 0.0;

        for (Cart cart : cartItems) {

            ProductVariation variation = cart.getProductVariation();

            if (!variation.getIsActive() || variation.getProduct().getIsDeleted())
                throw new RuntimeException("Invalid product variation");

            if (variation.getQuantity_available() < cart.getQuantity())
                throw new RuntimeException("Insufficient stock");

            totalAmount += variation.getPrice() * cart.getQuantity();
        }

        Order order = new Order();
        order.setCustomer(user);
        order.setAmountPaid(totalAmount);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setCustomerAddressAddressLine(address.getAddressLine());
        order.setCustomerAddressCity(address.getCity());
        order.setCustomerAddressState(address.getState());
        order.setCustomerAddressCountry(address.getCountry());
        order.setCustomerAddressZipCode(address.getZipCode());
        order.setCustomerAddressLabel(address.getLabel());

        order = orderRepository.save(order);

        for (Cart cart : cartItems) {

            ProductVariation variation = cart.getProductVariation();

            variation.setQuantity_available(
                    variation.getQuantity_available() - cart.getQuantity());

            productVariationRepository.save(variation);

            OrderProduct item = new OrderProduct();
            item.setOrder(order);
            item.setProductVariation(variation);
            item.setQuantity(cart.getQuantity());
            item.setPrice(variation.getPrice() * cart.getQuantity());
            orderProductRepository.save(item);

            OrderStatus status = new OrderStatus();
            status.setOrderProduct(item);
            status.setFromStatus(null);
            status.setToStatus(OrderState.ORDER_PLACED);
            status.setTransitionDate(LocalDateTime.now());

            orderStatusRepository.save(status);
        }

        cartRepository.deleteAll(cartItems);

        return new OrderResponse("Order placed successfully", order.getId());
    }

    @Transactional
    @Override
    public OrderResponse placePartialOrder(PartialOrderRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Address does not belong to user");
        }

        List<Cart> cartItems = cartRepository.findByCustomerAndIsWishlistItemFalse(user);

        Map<Long, Cart> cartMap = new HashMap<>();

        for (Cart cart : cartItems) {
            cartMap.put(cart.getProductVariation().getId(), cart);
        }

        double totalAmount = 0.0;

        for (Long variationId : request.getProductVariationIds()) {

            Cart cart = cartMap.get(variationId);
            if (cart == null)
                throw new RuntimeException("Item not found in cart");

            ProductVariation variation = cart.getProductVariation();

            if (!variation.getIsActive() || variation.getProduct().getIsDeleted())
                throw new RuntimeException("Invalid variation");

            if (variation.getQuantity_available() < cart.getQuantity())
                throw new RuntimeException("Insufficient stock");

            totalAmount += variation.getPrice() * cart.getQuantity();
        }

        Order order = new Order();
        order.setCustomer(user);
        order.setAmountPaid(totalAmount);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setCustomerAddressAddressLine(address.getAddressLine());
        order.setCustomerAddressCity(address.getCity());
        order.setCustomerAddressState(address.getState());
        order.setCustomerAddressCountry(address.getCountry());
        order.setCustomerAddressZipCode(address.getZipCode());
        order.setCustomerAddressLabel(address.getLabel());

        order = orderRepository.save(order);

        for (Long variationId : request.getProductVariationIds()) {

            Cart cart = cartMap.get(variationId);
            ProductVariation variation = cart.getProductVariation();

            variation.setQuantity_available(
                    variation.getQuantity_available() - cart.getQuantity());

            productVariationRepository.save(variation);

            OrderProduct item = new OrderProduct();
            item.setOrder(order);
            item.setProductVariation(variation);
            item.setQuantity(cart.getQuantity());
            item.setPrice(variation.getPrice()* cart.getQuantity());

            orderProductRepository.save(item);

            OrderStatus status = new OrderStatus();
            status.setOrderProduct(item);
            status.setFromStatus(null);
            status.setToStatus(OrderState.ORDER_PLACED);
            status.setTransitionDate(LocalDateTime.now());

            orderStatusRepository.save(status);

            cartRepository.delete(cart);
        }

        return new OrderResponse("Order placed successfully", order.getId());
    }
    @Transactional
    @Override
    public OrderResponse directOrder(OrderRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProductVariation variation = productVariationRepository.findById(request.getProductVariationId())
                .orElseThrow(() -> new RuntimeException("Invalid variation"));

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Address does not belong to user");
        }

        if (!variation.getIsActive() || variation.getProduct().getIsDeleted())
            throw new RuntimeException("Product not available");

        if (variation.getQuantity_available() < request.getQuantity())
            throw new RuntimeException("Insufficient stock");

        double totalAmount = variation.getPrice() * request.getQuantity();

        variation.setQuantity_available(variation.getQuantity_available() - request.getQuantity());

        productVariationRepository.save(variation);

        Order order = new Order();
        order.setCustomer(user);
        order.setAmountPaid(totalAmount);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setCustomerAddressAddressLine(address.getAddressLine());
        order.setCustomerAddressCity(address.getCity());
        order.setCustomerAddressState(address.getState());
        order.setCustomerAddressCountry(address.getCountry());
        order.setCustomerAddressZipCode(address.getZipCode());
        order.setCustomerAddressLabel(address.getLabel());

        order = orderRepository.save(order);

        OrderProduct item = new OrderProduct();
        item.setOrder(order);
        item.setProductVariation(variation);
        item.setQuantity(request.getQuantity());
        item.setPrice(variation.getPrice());

        orderProductRepository.save(item);
        OrderStatus status = new OrderStatus();
        status.setOrderProduct(item);
        status.setFromStatus(null);
        status.setToStatus(OrderState.ORDER_PLACED);
        status.setTransitionDate(LocalDateTime.now());

        orderStatusRepository.save(status);

        return new OrderResponse("Order placed successfully", order.getId());
    }

    @Transactional
    @Override
    public void cancelOrder(Long orderProductId) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        OrderProduct orderItem = orderProductRepository.findById(orderProductId)
                .orElseThrow(() -> new RuntimeException("Order item not found"));

        Order order = orderItem.getOrder();

        if (!order.getCustomer().getId().equals(user.getId()))
            throw new RuntimeException("Unauthorized cancellation");

        OrderStatus latestStatus = orderItem.getStatusHistory()
                .stream()
                .max(Comparator.comparing(OrderStatus::getTransitionDate))
                .orElseThrow(() -> new RuntimeException("No status found"));

        OrderState currentStatus = latestStatus.getToStatus();

        if (currentStatus != OrderState.ORDER_PLACED && currentStatus != OrderState.ORDER_CONFIRMED) {

            throw new RuntimeException("Cannot cancel at this stage");
        }

        OrderStatus cancelStatus = new OrderStatus();
        cancelStatus.setOrderProduct(orderItem);
        cancelStatus.setFromStatus(currentStatus);
        cancelStatus.setToStatus(OrderState.CANCELLED);
        cancelStatus.setTransitionDate(LocalDateTime.now());

        orderStatusRepository.save(cancelStatus);

        ProductVariation variation = orderItem.getProductVariation();
        variation.setQuantity_available(variation.getQuantity_available() + orderItem.getQuantity());

        productVariationRepository.save(variation);
    }

    @Transactional
    @Override
    public void returnOrder(Long orderProductId) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        OrderProduct orderProduct = orderProductRepository.findById(orderProductId)
                .orElseThrow(() -> new RuntimeException("Order product not found"));

        if (!orderProduct.getOrder().getCustomer().getId().equals(user.getId())) {
            throw new RuntimeException("Order does not belong to user");
        }

        OrderStatus latest = orderStatusRepository.findTopByOrderProductIdOrderByTransitionDateDesc(orderProductId);

        if (latest == null) {
            throw new RuntimeException("No order status found for this product");
        }

        if (latest.getToStatus() != OrderState.DELIVERED) {
            throw new RuntimeException("Return allowed only for delivered orders");
        }

        OrderStatus returnStatus = new OrderStatus();
        returnStatus.setOrderProduct(orderProduct);
        returnStatus.setFromStatus(latest.getToStatus());
        returnStatus.setToStatus(OrderState.RETURN_REQUESTED);
        returnStatus.setTransitionDate(LocalDateTime.now());

        orderStatusRepository.save(returnStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO viewMyOrder(Long orderId) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = orderRepository.findByIdAndCustomerId(orderId, user.getId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return toDto(order);
    }
    public OrderResponseDTO toDto(Order order) {

        List<OrderProductResponseDTO> products = order.getOrderProducts()
                .stream()
                .map(op -> {

                    String status = op.getStatusHistory()
                            .stream()
                            .max(Comparator.comparing(OrderStatus::getTransitionDate))
                            .map(os -> os.getToStatus().name())
                            .orElse("UNKNOWN");

                    return new OrderProductResponseDTO(
                            op.getId(),
                            status,
                            op.getQuantity(),
                            op.getPrice()
                    );

                }).toList();

        return new OrderResponseDTO(
                order.getId(),
                order.getAmountPaid(),
                order.getDateCreated(),
                products
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> listMyOrders(int max, int offset, String sort, String order, String query) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Sort.Direction direction = order.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(offset, max, Sort.by(direction, sort));

        Page<Order> orders;

        if (query != null && !query.isBlank()) {
            orders = orderRepository.searchMyOrders(user.getId(), query, pageable);
        } else {
            orders = orderRepository.findByCustomerId(user.getId(), pageable);
        }

        return orders.map(this::toDto);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> listOrdersOfMyProducts(int max, int offset, String sort, String order, String query) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User seller = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        Sort.Direction direction = order.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(offset, max, Sort.by(direction, sort));

        Page<Order> orders = orderRepository.findOrdersBySellerProducts(seller.getId(), query, pageable);

        return orders.map(this::toDto);
    }


    public Page<OrderResponseDTO> getAllOrdersAsAdmin(int max, int offset,
                                               String sort, String order,
                                               String query) {

        Sort.Direction direction =
                order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(offset, max, Sort.by(direction, sort));

        Page<Order> orders;

        if (query != null && !query.isEmpty()) {
            orders = orderRepository.searchOrders(query, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }

        return orders.map(this::mapToDTO);
    }

    private OrderResponseDTO mapToDTO(Order order) {

        List<OrderProductResponseDTO> products = order.getOrderProducts()
                .stream()
                .map(op -> {

                    String status = op.getStatusHistory()
                            .stream()
                            .max(Comparator.comparing(OrderStatus::getTransitionDate))
                            .map(os -> os.getToStatus().name())
                            .orElse("UNKNOWN");

                    return new OrderProductResponseDTO(
                            op.getId(),
                            status,
                            op.getQuantity(),
                            op.getPrice()
                    );

                }).toList();

        return new OrderResponseDTO(
                order.getId(),
                order.getAmountPaid(),
                order.getDateCreated(),
                products
        );
    }

}




