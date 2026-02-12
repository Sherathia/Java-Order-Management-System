package com.oms.dto;

import com.oms.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    
    private Long id;
    private String orderNumber;
    private Long userId;
    private String username;
    private List<OrderItemResponse> items;
    private String status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String billingAddress;
    private String paymentMethod;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime cancelledAt;
    
    public static OrderResponse fromEntity(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser().getId())
                .username(order.getUser().getUsername())
                .items(order.getOrderItems().stream()
                        .map(OrderItemResponse::fromEntity)
                        .collect(Collectors.toList()))
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .billingAddress(order.getBillingAddress())
                .paymentMethod(order.getPaymentMethod())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .cancelledAt(order.getCancelledAt())
                .build();
    }
}
