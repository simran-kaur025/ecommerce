package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.DTO.*;
import com.bootcamp.ecommerce.entity.*;
import com.bootcamp.ecommerce.enums.OrderState;
import com.bootcamp.ecommerce.enums.PaymentMethod;
import com.bootcamp.ecommerce.exceptionalHandler.BadRequestException;
import com.bootcamp.ecommerce.exceptionalHandler.InsufficientStockException;
import com.bootcamp.ecommerce.exceptionalHandler.ProductInactiveException;
import com.bootcamp.ecommerce.exceptionalHandler.ResourceNotFoundException;
import com.bootcamp.ecommerce.repository.*;
import com.bootcamp.ecommerce.service.EmailService;
import com.bootcamp.ecommerce.service.OrderService;
import com.bootcamp.ecommerce.specifications.OrderSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static com.bootcamp.ecommerce.enums.OrderState.ORDER_PLACED;



@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final ProductVariationRepository productVariationRepository;
    private final AddressRepository addressRepository;
    private final EmailService emailService;
    private final MessageSource messageSource;


    @Transactional
    @Override
    public OrderResponse placeOrderForCurrentUser(PlaceOrderRequest request, Locale locale) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Place order request received from user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Address does not belong to user");
        }

        List<Cart> cartItems = cartRepository.findByCustomerAndIsWishlistItemFalse(user);

        if (cartItems.isEmpty())
            throw new BadRequestException("Cart is empty");

        double totalAmount = 0.0;

        for (Cart cart : cartItems) {

            ProductVariation variation = cart.getProductVariation();

            if (!variation.getIsActive() || variation.getProduct().getIsDeleted())
                throw new ProductInactiveException("Invalid product variation");

            if (variation.getQuantity_available() < cart.getQuantity())
                throw new InsufficientStockException("Insufficient stock");

            totalAmount += variation.getPrice() * cart.getQuantity();
        }
        log.info("Total order amount for user {} calculated as {}", email, totalAmount);

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
        log.info("Order {} created for user {}", order.getId(), email);

        for (Cart cart : cartItems) {

            ProductVariation variation = cart.getProductVariation();

            variation.setQuantity_available(variation.getQuantity_available() - cart.getQuantity());

            productVariationRepository.save(variation);

            OrderProduct item = new OrderProduct();
            item.setOrder(order);
            item.setProductVariation(variation);
            item.setQuantity(cart.getQuantity());
            item.setPrice(variation.getPrice() * cart.getQuantity());
            item.setCurrentStatus(ORDER_PLACED);
            orderProductRepository.save(item);

            OrderStatus status = new OrderStatus();
            status.setOrderProduct(item);
            status.setFromStatus(null);
            status.setToStatus(OrderState.ORDER_PLACED);
            status.setTransitionDate(LocalDateTime.now());

            orderStatusRepository.save(status);
            log.info("Added OrderProduct {} to Order {} for user {}", item.getId(), order.getId(), email);
        }

        cartRepository.deleteAll(cartItems);
        String msg=messageSource.getMessage("order.success",null,locale);
        log.info("Order {} placed successfully for user {}", order.getId(), email);
        return new OrderResponse(msg, order.getId());
    }

    @Transactional
    @Override
    public OrderResponse placePartialOrder(PartialOrderRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Partial order request received from user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Address does not belong to user");
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
                throw new BadRequestException("Item not found in cart");

            ProductVariation variation = cart.getProductVariation();

            if (!variation.getIsActive() || variation.getProduct().getIsDeleted())
                throw new ProductInactiveException("Invalid variation");

            if (variation.getQuantity_available() < cart.getQuantity())
                throw new InsufficientStockException("Insufficient stock");

            totalAmount += variation.getPrice() * cart.getQuantity();
        }
        log.info("Total amount for partial order for user {} calculated as {}", email, totalAmount);

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
        log.info("Partial Order {} created for user {}", order.getId(), email);

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
            item.setCurrentStatus(ORDER_PLACED);

            orderProductRepository.save(item);

            OrderStatus status = new OrderStatus();
            status.setOrderProduct(item);
            status.setFromStatus(null);
            status.setToStatus(ORDER_PLACED);
            status.setTransitionDate(LocalDateTime.now());

            orderStatusRepository.save(status);

            cartRepository.delete(cart);
            log.info("Processed and removed ProductVariation {} from cart for user {}", variation.getId(), email);
        }
        log.info("Partial order {} placed successfully for user {}", order.getId(), email);
        return new OrderResponse("Order placed successfully", order.getId());
    }


    @Transactional
    @Override
    public OrderResponse directOrder(OrderRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Direct order request received from user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ProductVariation variation = productVariationRepository.findById(request.getProductVariationId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid variation"));

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Address does not belong to user");
        }

        if (!variation.getIsActive() || variation.getProduct().getIsDeleted())
            throw new ProductInactiveException("Product not available");

        if (variation.getQuantity_available() < request.getQuantity())
            throw new InsufficientStockException("Insufficient stock");

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
        log.info("Direct Order {} created for user {}", order.getId(), email);

        OrderProduct item = new OrderProduct();
        item.setOrder(order);
        item.setProductVariation(variation);
        item.setQuantity(request.getQuantity());
        item.setPrice(variation.getPrice());
        item.setCurrentStatus(ORDER_PLACED);
        log.info("OrderProduct {} added to Order {} for user {}", item.getId(), order.getId(), email);

        orderProductRepository.save(item);
        OrderStatus status = new OrderStatus();
        status.setOrderProduct(item);
        status.setFromStatus(null);
        status.setToStatus(OrderState.ORDER_PLACED);
        status.setTransitionDate(LocalDateTime.now());

        orderStatusRepository.save(status);
        log.info("Direct order {} placed successfully for user {}", order.getId(), email);

        return new OrderResponse("Order placed successfully", order.getId());
    }

    @Transactional
    @Override
    public void cancelOrder(Long orderProductId) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Cancel order request received from user: {} for OrderProduct {}", email, orderProductId);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        OrderProduct orderItem = orderProductRepository.findById(orderProductId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

        Order order = orderItem.getOrder();

        if (!order.getCustomer().getId().equals(user.getId()))
            throw new RuntimeException("Unauthorized cancellation");

        OrderState status = orderItem.getCurrentStatus();

        boolean cancellable = status == OrderState.ORDER_PLACED || status == OrderState.ORDER_CONFIRMED;

        if (!cancellable) {
            throw new RuntimeException("Cannot cancel at this stage");
        }

        OrderStatus cancelStatus = new OrderStatus();
        cancelStatus.setOrderProduct(orderItem);
        cancelStatus.setFromStatus(status);
        cancelStatus.setToStatus(OrderState.CANCELLED);
        cancelStatus.setTransitionDate(LocalDateTime.now());

        orderStatusRepository.save(cancelStatus);
        log.info("OrderProduct {} status updated to CANCELLED for user {}", orderProductId, email);

        ProductVariation variation = orderItem.getProductVariation();
        variation.setQuantity_available(variation.getQuantity_available() + orderItem.getQuantity());

        productVariationRepository.save(variation);
        log.info("OrderProduct {} successfully cancelled for user {}", orderProductId, email);
    }

    @Transactional
    @Override
    public void returnOrder(Long orderProductId) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Return order request received from user: {} for OrderProduct {}", email, orderProductId);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        OrderProduct orderProduct = orderProductRepository.findById(orderProductId)
                .orElseThrow(() -> new ResourceNotFoundException("Order product not found"));

        if (!orderProduct.getOrder().getCustomer().getId().equals(user.getId())) {
            throw new BadRequestException("Order does not belong to user");
        }

        OrderStatus latest = orderStatusRepository.findTopByOrderProductIdOrderByTransitionDateDesc(orderProductId);

        if (latest == null) {
            throw new ResourceNotFoundException("No order status found for this product");
        }

        if (latest.getToStatus() != OrderState.DELIVERED) {
            throw new BadRequestException("Return allowed only for delivered orders");
        }

        OrderStatus returnStatus = new OrderStatus();
        returnStatus.setOrderProduct(orderProduct);
        returnStatus.setFromStatus(latest.getToStatus());
        returnStatus.setToStatus(OrderState.RETURN_REQUESTED);
        returnStatus.setTransitionDate(LocalDateTime.now());

        orderStatusRepository.save(returnStatus);
        log.info("Return requested for OrderProduct {} by user {}", orderProductId, email);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO viewMyOrder(Long orderId) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("View order request received from user: {} for Order {}", email, orderId);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findByIdAndCustomerId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        log.info("Order {} fetched successfully for user {}", orderId, email);

        return toDto(order);
    }
    public OrderResponseDTO toDto(Order order) {

        List<OrderProductResponseDTO> products = order.getOrderProducts()
                .stream()
                .map(op -> {

                    String status = op.getCurrentStatus().name();
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
    public Page<OrderResponseDTO> listMyOrders(RequestParams requestParams) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("List orders request received from user: {}", email);

        Map<String, String> filters = new HashMap<>(requestParams.getFilters());

        filters.put("customerEmail", email);
        Specification<Order> specification = OrderSpecifications.extract(filters);

        Sort.Direction direction = Sort.Direction
                .fromOptionalString(requestParams.getOrder())
                .orElse(Sort.Direction.ASC);

        int pageNumber = requestParams.getOffset() / requestParams.getMax();

        Pageable pageable = PageRequest.of(
                pageNumber,
                requestParams.getMax(),
                Sort.by(direction, requestParams.getSortBy())
        );


        Page<Order> orders = orderRepository.findAll(specification, pageable);
        log.info("Fetched {} orders for user {}", orders.getNumberOfElements(), email);
        return orders.map(this::toDto);

    }


    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> listOrdersOfMyProducts(RequestParams requestParams) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("List orders of seller's products request received from user: {}", email);

        User seller = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        Map<String, String> filters = new HashMap<>(requestParams.getFilters());

        filters.put("sellerEmail", email);
        Specification<Order> specification = OrderSpecifications.extract(filters);

        Sort.Direction direction = Sort.Direction.fromOptionalString(requestParams.getOrder())
                .orElse(Sort.Direction.ASC);

        int pageNumber = requestParams.getOffset() / requestParams.getMax();

        Pageable pageable = PageRequest.of(
                pageNumber,
                requestParams.getMax(),
                Sort.by(direction, requestParams.getSortBy())
        );

        Page<Order> orders = orderRepository.findAll(specification, pageable);
        log.info("Fetched {} orders for seller {}", orders.getNumberOfElements(), email);

        return orders.map(this::toDto);

    }

    public Page<OrderResponseDTO> getAllOrdersAsAdmin(RequestParams requestParams) {
        log.info("Admin request received to list all orders");
        Map<String, String> filters = new HashMap<>(requestParams.getFilters());
        Specification<Order> specification = OrderSpecifications.extract(filters);

        Sort.Direction direction = Sort.Direction.fromOptionalString(requestParams.getOrder())
                .orElse(Sort.Direction.ASC);

        int pageNumber = requestParams.getOffset() / requestParams.getMax();

        Pageable pageable = PageRequest.of(pageNumber, requestParams.getMax(), Sort.by(direction, requestParams.getSortBy()));

        Page<Order> orders = orderRepository.findAll(specification, pageable);
        log.info("Admin fetched {} orders", orders.getNumberOfElements());

        return orders.map(this::toDto);
    }

    private OrderResponseDTO mapToDTO(Order order) {

        List<OrderProductResponseDTO> products = order.getOrderProducts()
                .stream()
                .map(op -> {

                    String status = op.getCurrentStatus().name();

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

    @Transactional
    @Override
    public void updateOrderStatus(UpdateOrderStatusRequest request, UserDetails userDetails) {

        log.info("Update order status request received for orderProductId: {}", request.getOrderProductId());
        OrderProduct orderProduct = orderProductRepository
                .findById(request.getOrderProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        String role = userDetails.getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        if (role.equals("ROLE_SELLER")) {

            String sellerEmail = userDetails.getUsername();

            String productSeller = orderProduct.getProductVariation()
                    .getProduct()
                    .getCreatedBy();

            if (!sellerEmail.equals(productSeller)) {
                throw new RuntimeException("Seller cannot update this order");
            }
        }

        OrderState currentStatus = orderProduct.getCurrentStatus();
        OrderState nextStatus = request.getToStatus();
        log.info("Current order status: {}, Requested status: {}", currentStatus, nextStatus);

        if (!OrderState.isValidTransition(currentStatus, nextStatus)) {
            throw new RuntimeException("Invalid status transition");
        }


        OrderStatus history = new OrderStatus();

        history.setOrderProduct(orderProduct);
        history.setFromStatus(currentStatus);
        history.setToStatus(nextStatus);

        orderStatusRepository.save(history);

        orderProduct.setCurrentStatus(nextStatus);

        orderProductRepository.save(orderProduct);
        log.info("Order status updated successfully for orderProductId {} from {} to {}",
                request.getOrderProductId(), currentStatus, nextStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public void notifyPendingOrders() {

        List<OrderProduct> pendingItems =
                orderProductRepository.findPendingSellerActionOrderProducts(ORDER_PLACED.name());

        if (pendingItems.isEmpty()) {
            return;
        }

        Map<Seller, List<OrderProduct>> itemsBySeller = new HashMap<>();

        for (OrderProduct item : pendingItems) {

            Seller seller = item.getProductVariation()
                    .getProduct()
                    .getSeller();

            if (!itemsBySeller.containsKey(seller)) {
                itemsBySeller.put(seller, new ArrayList<>());
            }

            itemsBySeller.get(seller).add(item);
        }

        for (Map.Entry<Seller, List<OrderProduct>> entry : itemsBySeller.entrySet()) {
            emailService.sendPendingOrdersReminder(entry.getKey(), entry.getValue());
        }
    }

}




