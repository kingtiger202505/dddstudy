package com.example.quotations.application.service;

import com.example.quotations.domain.model.Quotation;
import com.example.quotations.domain.model.ProductInventory;
import com.example.quotations.domain.repository.QuotationRepository;
import com.example.quotations.domain.repository.ProductInventoryRepository;
import com.example.quotations.domain.event.QuotationAcceptedEvent;
import com.example.quotations.application.dto.CreateQuotationCommand;
import com.example.quotations.application.dto.CounterOfferCommand;
import com.example.quotations.application.dto.AcceptQuotationCommand;
import com.example.quotations.application.dto.RejectQuotationCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 报价单应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuotationApplicationService {

    private final QuotationRepository quotationRepository;
    private final ProductInventoryRepository inventoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 创建报价单
     */
    @Transactional
    public Long createQuotation(CreateQuotationCommand command) {
        // 检查库存
        ProductInventory inventory = inventoryRepository.selectByProductId(command.getProductId());
        if (inventory == null) {
            throw new IllegalStateException("商品不存在或无库存: " + command.getProductId());
        }

        // 创建报价单聚合根
        Quotation quotation = new Quotation(
            command.getProductId(),
            command.getSellerId(),
            command.getBuyerId(),
            command.getOriginalPrice(),
            command.getQuotedPrice(),
            command.getExpiredAt()
        );

        // 保存
        quotationRepository.insert(quotation);
        log.info("创建报价单成功，ID: {}", quotation.getId());
        return quotation.getId();
    }

    /**
     * 买方还价
     */
    @Transactional
    public void counterOffer(CounterOfferCommand command) {
        Quotation quotation = quotationRepository.selectById(command.getQuotationId());
        if (quotation == null) {
            throw new IllegalArgumentException("报价单不存在: " + command.getQuotationId());
        }

        // 保存旧版本号用于乐观锁检查
        Integer oldVersion = quotation.getVersion();
        
        // 领域行为：还价
        quotation.counterOffer(command.getNewPrice(), command.getNewExpiredAt());

        // 乐观锁更新：使用旧版本号作为 WHERE 条件
        int updated = quotationRepository.updateWithOptimisticLock(quotation, oldVersion);
        if (updated == 0) {
            throw new OptimisticLockException("报价单已被修改，请刷新后重试");
        }

        log.info("还价成功，报价单ID: {}, 新价格: {}", command.getQuotationId(), command.getNewPrice());
    }

    /**
     * 卖方接受还价
     */
    @Transactional
    public void acceptCounterOffer(Long quotationId) {
        Quotation quotation = quotationRepository.selectById(quotationId);
        if (quotation == null) {
            throw new IllegalArgumentException("报价单不存在: " + quotationId);
        }

        quotation.acceptCounterOffer();

        // 保存旧版本号用于乐观锁检查
        Integer oldVersion = quotation.getVersion();

        int updated = quotationRepository.updateWithOptimisticLock(quotation, oldVersion);
        if (updated == 0) {
            throw new OptimisticLockException("报价单已被修改，请刷新后重试");
        }

        log.info("卖方接受还价成功，报价单ID: {}", quotationId);
    }

    /**
     * 卖方拒绝还价
     */
    @Transactional
    public void rejectCounterOffer(Long quotationId) {
        Quotation quotation = quotationRepository.selectById(quotationId);
        if (quotation == null) {
            throw new IllegalArgumentException("报价单不存在: " + quotationId);
        }

        quotation.rejectCounterOffer();

        // 保存旧版本号用于乐观锁检查
        Integer oldVersion = quotation.getVersion();

        int updated = quotationRepository.updateWithOptimisticLock(quotation, oldVersion);
        if (updated == 0) {
            throw new OptimisticLockException("报价单已被修改，请刷新后重试");
        }

        log.info("卖方拒绝还价成功，报价单ID: {}", quotationId);
    }

    /**
     * 买方接受报价 (核心流程：扣减库存 + 发布事件)
     */
    @Transactional
    public Long acceptQuotation(AcceptQuotationCommand command) {
        // 1. 查询报价单
        Quotation quotation = quotationRepository.selectById(command.getQuotationId());
        if (quotation == null) {
            throw new IllegalArgumentException("报价单不存在: " + command.getQuotationId());
        }

        // 2. 查询库存
        ProductInventory inventory = inventoryRepository.selectByProductId(quotation.getProductId());
        if (inventory == null) {
            throw new IllegalStateException("商品库存不存在: " + quotation.getProductId());
        }

        // 3. 领域行为：接受报价
        quotation.acceptQuotation();

        // 4. 保存旧版本号用于乐观锁检查
        Integer oldVersion = quotation.getVersion();

        // 5. 乐观锁更新报价单状态
        int updated = quotationRepository.updateWithOptimisticLock(quotation, oldVersion);
        if (updated == 0) {
            throw new OptimisticLockException("报价单已被修改，请刷新后重试");
        }

        // 5. CAS扣减库存 (带乐观锁)
        int deducted = inventoryRepository.deductStock(
            quotation.getProductId(), 
            command.getQuantity(), 
            inventory.getVersion()
        );
        if (deducted == 0) {
            // 库存不足或版本冲突，回滚报价单状态
            quotation.setStatus("INITIATED"); // 恢复状态
            throw new StockInsufficientException("库存不足或并发冲突，扣减失败");
        }

        // 6. 发布领域事件 (事务提交后由事务监听器处理)
        eventPublisher.publishEvent(new QuotationAcceptedEvent(
            quotation.getId(),
            quotation.getProductId(),
            quotation.getSellerId(),
            quotation.getBuyerId(),
            quotation.getQuotedPrice()
        ));

        log.info("接受报价成功，报价单ID: {}, 扣减数量: {}", command.getQuotationId(), command.getQuantity());
        return quotation.getId();
    }

    /**
     * 买方拒绝报价
     */
    @Transactional
    public void rejectQuotation(RejectQuotationCommand command) {
        Quotation quotation = quotationRepository.selectById(command.getQuotationId());
        if (quotation == null) {
            throw new IllegalArgumentException("报价单不存在: " + command.getQuotationId());
        }

        quotation.rejectQuotation();

        // 保存旧版本号用于乐观锁检查
        Integer oldVersion = quotation.getVersion();

        int updated = quotationRepository.updateWithOptimisticLock(quotation, oldVersion);
        if (updated == 0) {
            throw new OptimisticLockException("报价单已被修改，请刷新后重试");
        }

        log.info("拒绝报价成功，报价单ID: {}", command.getQuotationId());
    }

    /**
     * 查询报价单详情
     */
    public Quotation getQuotation(Long quotationId) {
        Quotation quotation = quotationRepository.selectById(quotationId);
        if (quotation == null) {
            throw new IllegalArgumentException("报价单不存在: " + quotationId);
        }
        
        // 检查是否过期
        quotation.checkExpiration();
        return quotation;
    }
    /**
     * 查询报价单列表
     */
    public List<Quotation> listQuotations() {
        return quotationRepository.findAll();
    }


    /**
     * 自定义异常：乐观锁冲突
     */
    public static class OptimisticLockException extends RuntimeException {
        public OptimisticLockException(String message) {
            super(message);
        }
    }

    /**
     * 自定义异常：库存不足
     */
    public static class StockInsufficientException extends RuntimeException {
        public StockInsufficientException(String message) {
            super(message);
        }
    }
}
