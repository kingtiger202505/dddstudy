package com.example.quotations.application.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 拒绝报价单命令
 */
@Data
public class RejectQuotationCommand {
    @NotNull(message = "报价单ID不能为空")
    private Long quotationId;
}
