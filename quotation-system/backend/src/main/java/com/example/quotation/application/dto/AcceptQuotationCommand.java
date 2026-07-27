package com.example.quotation.application.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 接受报价命令
 */
@Data
public class AcceptQuotationCommand {

    @NotNull(message = "报价单ID不能为空")
    private Long quotationId;

    @NotNull(message = "购买数量不能为空")
    private Integer quantity;
}
