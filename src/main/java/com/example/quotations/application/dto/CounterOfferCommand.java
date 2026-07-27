package com.example.quotations.application.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 还价命令
 */
@Data
public class CounterOfferCommand {
    @NotNull(message = "报价单ID不能为空")
    private Long quotationId;

    @NotNull(message = "新价格不能为空")
    private BigDecimal newPrice;

    @NotNull(message = "新过期时间不能为空")
    private LocalDateTime newExpiredAt;
}
