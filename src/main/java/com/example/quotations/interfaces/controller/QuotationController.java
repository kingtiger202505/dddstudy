package com.example.quotations.interfaces.controller;

import com.example.quotations.application.service.QuotationApplicationService;
import com.example.quotations.application.dto.CreateQuotationCommand;
import com.example.quotations.application.dto.CounterOfferCommand;
import com.example.quotations.application.dto.AcceptQuotationCommand;
import com.example.quotations.application.dto.RejectQuotationCommand;
import com.example.quotations.domain.model.Quotation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 报价单 REST 控制器
 */
@RestController
@RequestMapping("/api/quotations")
@RequiredArgsConstructor
public class QuotationController {

    private final QuotationApplicationService quotationService;

    /**
     * 创建报价单
     */
    @PostMapping
    public ResponseEntity<Long> createQuotation(@Valid @RequestBody CreateQuotationCommand command) {
        Long quotationId = quotationService.createQuotation(command);
        return ResponseEntity.ok(quotationId);
    }

    /**
     * 买方还价
     */
    @PostMapping("/{quotationId}/counter-offer")
    public ResponseEntity<Void> counterOffer(@PathVariable Long quotationId,
                                              @Valid @RequestBody CounterOfferCommand command) {
        command.setQuotationId(quotationId);
        quotationService.counterOffer(command);
        return ResponseEntity.ok().build();
    }

    /**
     * 卖方接受还价
     */
    @PostMapping("/{quotationId}/accept-counter")
    public ResponseEntity<Void> acceptCounterOffer(@PathVariable Long quotationId) {
        quotationService.acceptCounterOffer(quotationId);
        return ResponseEntity.ok().build();
    }

    /**
     * 卖方拒绝还价
     */
    @PostMapping("/{quotationId}/reject-counter")
    public ResponseEntity<Void> rejectCounterOffer(@PathVariable Long quotationId) {
        quotationService.rejectCounterOffer(quotationId);
        return ResponseEntity.ok().build();
    }

    /**
     * 买方接受报价
     */
    @PostMapping("/{quotationId}/accept")
    public ResponseEntity<Long> acceptQuotation(@PathVariable Long quotationId,
                                                 @RequestParam Integer quantity) {
        AcceptQuotationCommand command = new AcceptQuotationCommand();
        command.setQuotationId(quotationId);
        command.setQuantity(quantity);
        Long resultId = quotationService.acceptQuotation(command);
        return ResponseEntity.ok(resultId);
    }

    /**
     * 买方拒绝报价
     */
    @PostMapping("/{quotationId}/reject")
    public ResponseEntity<Void> rejectQuotation(@PathVariable Long quotationId,
                                                 @Valid @RequestBody RejectQuotationCommand command) {
        command.setQuotationId(quotationId);
        quotationService.rejectQuotation(command);
        return ResponseEntity.ok().build();
    }

    /**
     * 查询报价单详情
     */
    @GetMapping("/{quotationId}")
    public ResponseEntity<Quotation> getQuotation(@PathVariable Long quotationId) {
        Quotation quotation = quotationService.getQuotation(quotationId);
        return ResponseEntity.ok(quotation);
    }

    /**
     * 查询报价单列表
     */
    @GetMapping
    public ResponseEntity<List<Quotation>> listQuotations() {
        List<Quotation> quotations = quotationService.listQuotations();
        return ResponseEntity.ok(quotations);
    }
}
