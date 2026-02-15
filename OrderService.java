package com.oms.service;

import com.oms.dto.OrderRequest;
import com.oms.dto.OrderResponse;
import com.oms.entity.Order;
import com.oms.entity.OrderItem;
import com.oms.entity.User;
import com.oms.exception.ResourceNotFoundException;
import com.oms.exception.UnauthorizedException;
import com.oms.repository.OrderRepository;
import com.oms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        User currentUser = getCurrentUser();
        log.info("Creating order for user: {}", currentUser.getUsername());
        
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(currentUser)
                .status(Order.OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .billingAddress(request.getBillingAddress())
                .paymentMethod(request.getPaymentMethod())
                .notes(request.getNotes())
                .build();
        
        request.getItems().forEach(itemRequest -> {
            OrderItem orderItem = OrderItem.builder()
                    .productName(itemRequest.getProductName())
                    .productCode(itemRequest.getProductCode())
                    .quantity(itemRequest.getQuantity())
                    .price(itemRequest.getPrice())
                    .discount(itemRequest.getDiscount() != null ? itemRequest.getDiscount() : BigDecimal.ZERO)
                    .description(itemRequest.getDescription())
                    .build();
            order.addOrderItem(orderItem);
        });
        
        order.calculateTotalAmount();
        
        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with number: {}", savedOrder.getOrderNumber());
        
        return OrderResponse.fromEntity(savedOrder);
    }
    
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {
        User currentUser = getCurrentUser();
        log.info("Fetching orders for user: {}", currentUser.getUsername());
        
        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(currentUser);
        return orders.stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        User currentUser = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to view this order");
        }
        
        return OrderResponse.fromEntity(order);
    }
    
    @Transactional
    public OrderResponse updateOrder(Long orderId, OrderRequest request) {
        User currentUser = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to update this order");
        }
        
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be updated");
        }
        
        log.info("Updating order: {}", order.getOrderNumber());
        
        // Clear existing items and add new ones
        order.getOrderItems().clear();
        
        request.getItems().forEach(itemRequest -> {
            OrderItem orderItem = OrderItem.builder()
                    .productName(itemRequest.getProductName())
                    .productCode(itemRequest.getProductCode())
                    .quantity(itemRequest.getQuantity())
                    .price(itemRequest.getPrice())
                    .discount(itemRequest.getDiscount() != null ? itemRequest.getDiscount() : BigDecimal.ZERO)
                    .description(itemRequest.getDescription())
                    .build();
            order.addOrderItem(orderItem);
        });
        
        order.setShippingAddress(request.getShippingAddress());
        order.setBillingAddress(request.getBillingAddress());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setNotes(request.getNotes());
        order.calculateTotalAmount();
        
        Order updatedOrder = orderRepository.save(order);
        log.info("Order updated successfully: {}", updatedOrder.getOrderNumber());
        
        return OrderResponse.fromEntity(updatedOrder);
    }
    
    @Transactional
    public void cancelOrder(Long orderId) {
        User currentUser = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to cancel this order");
        }
        
        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled");
        }
        
        if (order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new IllegalStateException("Delivered orders cannot be cancelled");
        }
        
        log.info("Cancelling order: {}", order.getOrderNumber());
        
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        orderRepository.save(order);
        
        log.info("Order cancelled successfully: {}", order.getOrderNumber());
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
    
    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
