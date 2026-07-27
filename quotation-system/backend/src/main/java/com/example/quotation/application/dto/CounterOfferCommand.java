package com.example.quotation.application.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 还价命令
 */
@Data
public class CounterOfferCommand {

    @NotNull(message = "报价单ID不能为空")
    private Long quotationId;

    @NotNull(message = "还价价格不能为空")
    private BigDecimal offerPrice;
}
