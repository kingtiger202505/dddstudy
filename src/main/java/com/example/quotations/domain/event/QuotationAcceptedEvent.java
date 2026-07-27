package com.example.quotations.domain.event;

import lombok.Getter;
import java.math.BigDecimal;

/**
 * 报价单已接受事件
 */
@Getter
public class QuotationAcceptedEvent extends DomainEvent {
    private final Long quotationId;
    private final Long productId;
    private final Long sellerId;
    private final Long buyerId;
    private final BigDecimal finalPrice;

    public QuotationAcceptedEvent(Long quotationId, Long productId, Long sellerId, 
                                   Long buyerId, BigDecimal finalPrice) {
        super();
        this.quotationId = quotationId;
        this.productId = productId;
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.finalPrice = finalPrice;
    }
}
