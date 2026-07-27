package com.example.quotation.application.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 创建报价单命令
 */
@Data
public class CreateQuotationCommand {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "商品名称不能为空")
    private String productName;

    @NotNull(message = "原始价格不能为空")
    private BigDecimal originalPrice;

    @NotNull(message = "最低可接受价格不能为空")
    private BigDecimal minAcceptablePrice;

    @NotNull(message = "库存数量不能为空")
    private Integer stockQuantity;
}
