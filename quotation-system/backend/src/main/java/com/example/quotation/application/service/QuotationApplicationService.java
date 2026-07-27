package com.example.quotation.application.service;

import com.example.quotation.application.dto.*;
import com.example.quotation.domain.event.QuotationAcceptedEvent;
import com.example.quotation.domain.model.Quotation;
import com.example.quotation.domain.repository.QuotationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 报价单应用服务
 */
@Service
@RequiredArgsConstructor
public class QuotationApplicationService {

    private final QuotationRepository quotationRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 创建报价单
     */
    @Transactional
    public Long createQuotation(CreateQuotationCommand command) {
        Quotation quotation = new Quotation();
        quotation.setProductId(command.getProductId());
        quotation.setProductName(command.getProductName());
        quotation.setOriginalPrice(command.getOriginalPrice());
        quotation.setCurrentPrice(command.getOriginalPrice());
        quotation.setMinAcceptablePrice(command.getMinAcceptablePrice());
        quotation.setStockQuantity(command.getStockQuantity());
        quotation.setStatus("PENDING");
        quotation.setVersion(1);

        quotationRepository.insert(quotation);
        return quotation.getId();
    }

    /**
     * 买家还价
     */
    @Transactional
    public void counterOffer(CounterOfferCommand command) {
        Quotation quotation = quotationRepository.selectById(command.getQuotationId());
        if (quotation == null) {
            throw new IllegalArgumentException("报价单不存在: " + command.getQuotationId());
        }

        Integer originalVersion = quotation.getVersion();
        quotation.counterOffer(command.getOfferPrice());

        int updated = quotationRepository.updateWithOptimisticLock(quotation, originalVersion);
        if (updated == 0) {
            throw new IllegalStateException("报价单已被修改，请刷新后重试");
        }
    }

    /**
     * 卖家接受还价
     */
    @Transactional
    public void acceptCounterOffer(Long quotationId) {
        Quotation quotation = quotationRepository.selectById(quotationId);
        if (quotation == null) {
            throw new IllegalArgumentException("报价单不存在: " + quotationId);
        }

        Integer originalVersion = quotation.getVersion();
        quotation.acceptCounterOffer();

        int updated = quotationRepository.updateWithOptimisticLock(quotation, originalVersion);
        if (updated == 0) {
            throw new IllegalStateException("报价单已被修改，请刷新后重试");
        }
    }

    /**
     * 卖家拒绝还价
     */
    @Transactional
    public void rejectCounterOffer(Long quotationId) {
        Quotation quotation = quotationRepository.selectById(quotationId);
        if (quotation == null) {
            throw new IllegalArgumentException("报价单不存在: " + quotationId);
        }

        Integer originalVersion = quotation.getVersion();
        quotation.rejectCounterOffer();

        int updated = quotationRepository.updateWithOptimisticLock(quotation, originalVersion);
        if (updated == 0) {
            throw new IllegalStateException("报价单已被修改，请刷新后重试");
        }
    }

    /**
     * 买家接受报价（扣减库存）
     */
    @Transactional
    public void acceptQuotation(AcceptQuotationCommand command) {
        Quotation quotation = quotationRepository.selectById(command.getQuotationId());
        if (quotation == null) {
            throw new IllegalArgumentException("报价单不存在: " + command.getQuotationId());
        }

        Integer originalVersion = quotation.getVersion();
        quotation.acceptQuotation();

        // 先更新状态
        int updated = quotationRepository.updateWithOptimisticLock(quotation, originalVersion);
        if (updated == 0) {
            throw new IllegalStateException("报价单已被修改，请刷新后重试");
        }

        // CAS方式扣减库存
        int stockUpdated = quotationRepository.decreaseStockCas(
            quotation.getId(), 
            command.getQuantity(), 
            quotation.getVersion() // 使用更新后的版本号
        );
        if (stockUpdated == 0) {
            throw new IllegalStateException("库存不足或数据已被修改");
        }

        // 发布领域事件
        eventPublisher.publishEvent(new QuotationAcceptedEvent(
            quotation.getId(),
            quotation.getProductId(),
            quotation.getProductName(),
            quotation.getCurrentPrice(),
            command.getQuantity()
        ));
    }

    /**
     * 卖家拒绝报价
     */
    @Transactional
    public void rejectQuotation(Long quotationId) {
        Quotation quotation = quotationRepository.selectById(quotationId);
        if (quotation == null) {
            throw new IllegalArgumentException("报价单不存在: " + quotationId);
        }

        Integer originalVersion = quotation.getVersion();
        quotation.rejectQuotation();

        int updated = quotationRepository.updateWithOptimisticLock(quotation, originalVersion);
        if (updated == 0) {
            throw new IllegalStateException("报价单已被修改，请刷新后重试");
        }
    }

    /**
     * 查询报价单详情
     */
    public Quotation getQuotation(Long quotationId) {
        Quotation quotation = quotationRepository.selectById(quotationId);
        if (quotation == null) {
            throw new IllegalArgumentException("报价单不存在: " + quotationId);
        }
        return quotation;
    }
}
