package com.oms.dto;

import com.oms.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
    
    private Long id;
    private String productName;
    private String productCode;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal discount;
    private BigDecimal subtotal;
    private String description;
    
    public static OrderItemResponse fromEntity(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productName(item.getProductName())
                .productCode(item.getProductCode())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .discount(item.getDiscount())
                .subtotal(item.getSubtotal())
                .description(item.getDescription())
                .build();
    }
}
