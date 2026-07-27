package com.example.quotations.application.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建报价单命令
 */
@Data
public class CreateQuotationCommand {
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "卖方ID不能为空")
    private Long sellerId;

    @NotNull(message = "买方ID不能为空")
    private Long buyerId;

    @NotNull(message = "原价不能为空")
    private BigDecimal originalPrice;

    @NotNull(message = "报价不能为空")
    private BigDecimal quotedPrice;

    @NotNull(message = "过期时间不能为空")
    private LocalDateTime expiredAt;
}
