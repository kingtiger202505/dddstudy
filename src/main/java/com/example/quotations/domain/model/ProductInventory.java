package com.example.quotations.domain.model;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 商品库存聚合根
 */
@Data
public class ProductInventory {
    private Long id;
    private Long productId;
    private Integer stockQuantity;
    private BigDecimal unitPrice;
    private Integer version; // 乐观锁版本号

    public ProductInventory() {}

    public ProductInventory(Long productId, Integer stockQuantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.stockQuantity = stockQuantity;
        this.unitPrice = unitPrice;
    }

    /**
     * 扣减库存 (CAS操作)
     * @param quantity 扣减数量
     * @return 是否扣减成功
     */
    public boolean tryDeductStock(int quantity) {
        if (this.stockQuantity < quantity) {
            return false;
        }
        this.stockQuantity -= quantity;
        return true;
    }

    /**
     * 恢复库存
     */
    public void restoreStock(int quantity) {
        this.stockQuantity += quantity;
    }
}
