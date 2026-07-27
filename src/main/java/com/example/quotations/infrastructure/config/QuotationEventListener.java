package com.example.quotations.infrastructure.config;

import com.example.quotations.domain.event.QuotationAcceptedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 领域事件监听器
 * 用于处理报价单接受后的后续操作（如发送通知、记录日志等）
 */
@Slf4j
@Component
public class QuotationEventListener {

    /**
     * 监听报价单接受事件
     * 在事务提交后异步执行，保证数据一致性
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleQuotationAccepted(QuotationAcceptedEvent event) {
        log.info("收到报价单接受事件：quotationId={}, productId={}, sellerId={}, buyerId={}, finalPrice={}",
                event.getQuotationId(),
                event.getProductId(),
                event.getSellerId(),
                event.getBuyerId(),
                event.getFinalPrice());

        // 这里可以执行各种后续操作：
        // 1. 发送站内通知给买卖双方
        // 2. 发送短信/邮件通知
        // 3. 记录操作日志
        // 4. 触发订单创建流程
        // 5. 更新统计数据
        
        sendNotification(event);
        logAuditLog(event);
    }

    /**
     * 发送通知
     */
    private void sendNotification(QuotationAcceptedEvent event) {
        log.info("发送通知：报价单{}已被接受，最终价格{}", event.getQuotationId(), event.getFinalPrice());
        // TODO: 实现具体的通知逻辑
    }

    /**
     * 记录审计日志
     */
    private void logAuditLog(QuotationAcceptedEvent event) {
        log.info("审计日志：用户{}接受了报价单{}, 商品{}, 价格{}", 
                event.getBuyerId(), event.getQuotationId(), event.getProductId(), event.getFinalPrice());
        // TODO: 持久化到审计日志表
    }
}
