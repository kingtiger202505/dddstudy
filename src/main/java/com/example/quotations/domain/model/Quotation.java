package com.example.quotations.domain.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报价单聚合根
 */
@Data
public class Quotation {
    private Long id;
    private Long productId;
    private Long sellerId;
    private Long buyerId;
    private BigDecimal originalPrice;
    private BigDecimal quotedPrice;
    private String status; // INITIATED, COUNTERED, ACCEPTED, REJECTED, EXPIRED
    private Integer version; // 乐观锁版本号
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiredAt;

    public Quotation() {}

    public Quotation(Long productId, Long sellerId, Long buyerId, BigDecimal originalPrice, 
                     BigDecimal quotedPrice, LocalDateTime expiredAt) {
        this.productId = productId;
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.originalPrice = originalPrice;
        this.quotedPrice = quotedPrice;
        this.status = "INITIATED";
        this.expiredAt = expiredAt;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 买方还价
     */
    public void counterOffer(BigDecimal newPrice, LocalDateTime newExpiredAt) {
        if (!"INITIATED".equals(this.status) && !"COUNTERED".equals(this.status)) {
            throw new IllegalStateException("当前状态不允许还价: " + this.status);
        }
        if (LocalDateTime.now().isAfter(this.expiredAt)) {
            throw new IllegalStateException("报价已过期");
        }
        
        this.quotedPrice = newPrice;
        this.status = "COUNTERED";
        this.expiredAt = newExpiredAt;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 卖方接受还价
     */
    public void acceptCounterOffer() {
        if (!"COUNTERED".equals(this.status)) {
            throw new IllegalStateException("当前状态不允许接受: " + this.status);
        }
        if (LocalDateTime.now().isAfter(this.expiredAt)) {
            throw new IllegalStateException("报价已过期");
        }
        
        this.status = "ACCEPTED";
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 卖方拒绝还价
     */
    public void rejectCounterOffer() {
        if (!"COUNTERED".equals(this.status)) {
            throw new IllegalStateException("当前状态不允许拒绝: " + this.status);
        }
        
        this.status = "REJECTED";
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 买方直接接受报价
     */
    public void acceptQuotation() {
        if (!"INITIATED".equals(this.status)) {
            throw new IllegalStateException("当前状态不允许接受: " + this.status);
        }
        if (LocalDateTime.now().isAfter(this.expiredAt)) {
            throw new IllegalStateException("报价已过期");
        }
        
        this.status = "ACCEPTED";
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 买方拒绝报价
     */
    public void rejectQuotation() {
        if (!"INITIATED".equals(this.status)) {
            throw new IllegalStateException("当前状态不允许拒绝: " + this.status);
        }
        
        this.status = "REJECTED";
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 过期检查
     */
    public void checkExpiration() {
        if (LocalDateTime.now().isAfter(this.expiredAt) && 
            !"ACCEPTED".equals(this.status) && 
            !"REJECTED".equals(this.status)) {
            this.status = "EXPIRED";
            this.updatedAt = LocalDateTime.now();
        }
    }
}
