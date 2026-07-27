package com.example.quotation.interfaces.web;

import com.example.quotation.application.dto.*;
import com.example.quotation.application.service.QuotationApplicationService;
import com.example.quotation.domain.model.Quotation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 报价单 REST API 控制器
 */
@RestController
@RequestMapping("/api/quotation")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 允许跨域，方便 H5/小程序调用
public class QuotationController {

    private final QuotationApplicationService quotationService;

    /**
     * 创建报价单
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createQuotation(@Valid @RequestBody CreateQuotationCommand command) {
        Long quotationId = quotationService.createQuotation(command);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", quotationId);
        result.put("message", "报价单创建成功");
        
        return ResponseEntity.ok(result);
    }

    /**
     * 查询报价单详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getQuotation(@PathVariable Long id) {
        Quotation quotation = quotationService.getQuotation(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", quotation);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 买家还价
     */
    @PostMapping("/counter-offer")
    public ResponseEntity<Map<String, Object>> counterOffer(@Valid @RequestBody CounterOfferCommand command) {
        quotationService.counterOffer(command);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "还价成功");
        
        return ResponseEntity.ok(result);
    }

    /**
     * 卖家接受还价
     */
    @PostMapping("/{id}/accept-counter-offer")
    public ResponseEntity<Map<String, Object>> acceptCounterOffer(@PathVariable Long id) {
        quotationService.acceptCounterOffer(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "已接受还价");
        
        return ResponseEntity.ok(result);
    }

    /**
     * 卖家拒绝还价
     */
    @PostMapping("/{id}/reject-counter-offer")
    public ResponseEntity<Map<String, Object>> rejectCounterOffer(@PathVariable Long id) {
        quotationService.rejectCounterOffer(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "已拒绝还价");
        
        return ResponseEntity.ok(result);
    }

    /**
     * 买家接受报价
     */
    @PostMapping("/{id}/accept")
    public ResponseEntity<Map<String, Object>> acceptQuotation(
            @PathVariable Long id, 
            @Valid @RequestBody AcceptQuotationCommand command) {
        command.setQuotationId(id);
        quotationService.acceptQuotation(command);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "报价已接受，订单生成中");
        
        return ResponseEntity.ok(result);
    }

    /**
     * 卖家拒绝报价
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectQuotation(@PathVariable Long id) {
        quotationService.rejectQuotation(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "已拒绝报价");
        
        return ResponseEntity.ok(result);
    }
}
