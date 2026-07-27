package com.example.quotation.domain.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 报价单事件监听器
 * 处理异步副作用逻辑（如发送通知、记录日志等）
 */
@Slf4j
@Component
public class QuotationEventListener {

    /**
     * 监听报价单已接受事件
     * 异步执行，不影响主事务
     */
    @Async
    @EventListener
    public void handleQuotationAccepted(QuotationAcceptedEvent event) {
        log.info("=== 收到报价单已接受事件 ===");
        log.info("报价单ID: {}", event.getQuotationId());
        log.info("商品ID: {}, 名称: {}", event.getProductId(), event.getProductName());
        log.info("成交价格: {}, 数量: {}", event.getFinalPrice(), event.getQuantity());
        log.info("事件发生时间: {}", event.getOccurredOn());
        
        // TODO: 在这里添加后续业务逻辑
        // 1. 发送短信/微信通知
        // 2. 生成订单
        // 3. 记录操作日志
        // 4. 触发物流流程
        // 等等...
        
        log.info("=== 事件处理完成 ===");
    }
}
