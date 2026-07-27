package com.example.quotation.domain.event;

import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报价单已接受事件
 */
@Getter
public class QuotationAcceptedEvent extends DomainEvent {
    private final Long quotationId;
    private final Long productId;
    private final String productName;
    private final BigDecimal finalPrice;
    private final Integer quantity;

    public QuotationAcceptedEvent(Long quotationId, Long productId, String productName, 
                                   BigDecimal finalPrice, Integer quantity) {
        super(LocalDateTime.now());
        this.quotationId = quotationId;
        this.productId = productId;
        this.productName = productName;
        this.finalPrice = finalPrice;
        this.quantity = quantity;
    }
}
