package com.example.quotation.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报价单聚合根
 */
@Data
@TableName("quotation")
public class Quotation {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 原始价格
     */
    private BigDecimal originalPrice;

    /**
     * 当前报价
     */
    private BigDecimal currentPrice;

    /**
     * 最低可接受价格
     */
    private BigDecimal minAcceptablePrice;

    /**
     * 库存数量
     */
    private Integer stockQuantity;

    /**
     * 状态：PENDING(待处理), COUNTER_OFFER(已还价), ACCEPTED(已接受), REJECTED(已拒绝)
     */
    private String status;

    /**
     * 版本号（乐观锁）
     */
    @Version
    private Integer version;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;

    // ==================== 业务行为方法 ====================

    /**
     * 买家还价
     */
    public void counterOffer(BigDecimal offerPrice) {
        if (offerPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("还价必须大于0");
        }
        if (!"PENDING".equals(this.status) && !"COUNTER_OFFER".equals(this.status)) {
            throw new IllegalStateException("当前状态不能还价: " + this.status);
        }
        this.currentPrice = offerPrice;
        this.status = "COUNTER_OFFER";
    }

    /**
     * 卖家接受还价
     */
    public void acceptCounterOffer() {
        if (!"COUNTER_OFFER".equals(this.status)) {
            throw new IllegalStateException("当前状态不能接受还价: " + this.status);
        }
        if (this.currentPrice.compareTo(this.minAcceptablePrice) < 0) {
            throw new IllegalStateException("还价低于最低可接受价格");
        }
        this.status = "ACCEPTED";
    }

    /**
     * 卖家拒绝还价
     */
    public void rejectCounterOffer() {
        if (!"COUNTER_OFFER".equals(this.status)) {
            throw new IllegalStateException("当前状态不能拒绝还价: " + this.status);
        }
        this.status = "REJECTED";
    }

    /**
     * 买家接受报价
     */
    public void acceptQuotation() {
        if (!"PENDING".equals(this.status) && !"COUNTER_OFFER".equals(this.status)) {
            throw new IllegalStateException("当前状态不能接受报价: " + this.status);
        }
        this.status = "ACCEPTED";
    }

    /**
     * 卖家拒绝报价
     */
    public void rejectQuotation() {
        if (!"PENDING".equals(this.status) && !"COUNTER_OFFER".equals(this.status)) {
            throw new IllegalStateException("当前状态不能拒绝报价: " + this.status);
        }
        this.status = "REJECTED";
    }

    /**
     * 扣减库存（CAS方式）
     */
    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("扣减数量必须大于0");
        }
        if (this.stockQuantity < quantity) {
            throw new IllegalStateException("库存不足");
        }
        this.stockQuantity -= quantity;
    }
}
