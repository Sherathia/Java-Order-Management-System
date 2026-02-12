package com.oms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "ORDER_ITEMS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_item_seq")
    @SequenceGenerator(name = "order_item_seq", sequenceName = "ORDER_ITEM_SEQ", allocationSize = 1)
    @Column(name = "ORDER_ITEM_ID")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_ID", nullable = false)
    private Order order;
    
    @Column(name = "PRODUCT_NAME", nullable = false, length = 200)
    private String productName;
    
    @Column(name = "PRODUCT_CODE", length = 50)
    private String productCode;
    
    @Column(name = "QUANTITY", nullable = false)
    private Integer quantity;
    
    @Column(name = "PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "DISCOUNT", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;
    
    @Column(name = "DESCRIPTION", length = 500)
    private String description;
    
    public BigDecimal getSubtotal() {
        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity));
        if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
            subtotal = subtotal.subtract(discount);
        }
        return subtotal;
    }
}
