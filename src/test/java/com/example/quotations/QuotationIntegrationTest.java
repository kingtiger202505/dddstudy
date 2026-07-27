package com.example.quotations;

import com.example.quotations.application.dto.AcceptQuotationCommand;
import com.example.quotations.application.dto.CounterOfferCommand;
import com.example.quotations.application.dto.CreateQuotationCommand;
import com.example.quotations.application.dto.RejectQuotationCommand;
import com.example.quotations.application.service.QuotationApplicationService;
import com.example.quotations.domain.model.Quotation;
import com.example.quotations.domain.repository.ProductInventoryRepository;
import com.example.quotations.domain.repository.QuotationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 报价单服务集成测试
 */
@SpringBootTest
public class QuotationIntegrationTest {

    @Autowired
    private QuotationApplicationService quotationService;

    @Autowired
    private QuotationRepository quotationRepository;

    @Autowired
    private ProductInventoryRepository inventoryRepository;

    private Long testProductId = 1001L;
    private Long testSellerId = 1L;
    private Long testBuyerId = 2L;

    @BeforeEach
    void setUp() {
        // 确保库存存在
        if (inventoryRepository.selectByProductId(testProductId) == null) {
            throw new IllegalStateException("测试商品库存不存在");
        }
    }

    @Test
    void testCreateQuotation() {
        // Given
        CreateQuotationCommand command = new CreateQuotationCommand();
        command.setProductId(testProductId);
        command.setSellerId(testSellerId);
        command.setBuyerId(testBuyerId);
        command.setOriginalPrice(new BigDecimal("100.00"));
        command.setQuotedPrice(new BigDecimal("90.00"));
        command.setExpiredAt(LocalDateTime.now().plusHours(24));

        // When
        Long quotationId = quotationService.createQuotation(command);

        // Then
        assertNotNull(quotationId);
        Quotation quotation = quotationService.getQuotation(quotationId);
        assertEquals("INITIATED", quotation.getStatus());
        assertEquals(testProductId, quotation.getProductId());
        assertEquals(new BigDecimal("90.00"), quotation.getQuotedPrice());
    }

    @Test
    void testCounterOffer() {
        // Given: 创建报价单
        CreateQuotationCommand createCommand = new CreateQuotationCommand();
        createCommand.setProductId(testProductId);
        createCommand.setSellerId(testSellerId);
        createCommand.setBuyerId(testBuyerId);
        createCommand.setOriginalPrice(new BigDecimal("100.00"));
        createCommand.setQuotedPrice(new BigDecimal("90.00"));
        createCommand.setExpiredAt(LocalDateTime.now().plusHours(24));
        Long quotationId = quotationService.createQuotation(createCommand);

        // When: 买方还价
        CounterOfferCommand counterCommand = new CounterOfferCommand();
        counterCommand.setQuotationId(quotationId);
        counterCommand.setNewPrice(new BigDecimal("85.00"));
        counterCommand.setNewExpiredAt(LocalDateTime.now().plusHours(48));
        quotationService.counterOffer(counterCommand);

        // Then
        Quotation quotation = quotationService.getQuotation(quotationId);
        assertEquals("COUNTERED", quotation.getStatus());
        assertEquals(new BigDecimal("85.00"), quotation.getQuotedPrice());
    }

    @Test
    void testAcceptQuotationWithStockDeduction() {
        // Given: 创建报价单
        CreateQuotationCommand createCommand = new CreateQuotationCommand();
        createCommand.setProductId(testProductId);
        createCommand.setSellerId(testSellerId);
        createCommand.setBuyerId(testBuyerId);
        createCommand.setOriginalPrice(new BigDecimal("100.00"));
        createCommand.setQuotedPrice(new BigDecimal("90.00"));
        createCommand.setExpiredAt(LocalDateTime.now().plusHours(24));
        Long quotationId = quotationService.createQuotation(createCommand);

        // Get initial stock
        Integer initialStock = inventoryRepository.selectByProductId(testProductId).getStockQuantity();

        // When: 买方接受报价
        AcceptQuotationCommand acceptCommand = new AcceptQuotationCommand();
        acceptCommand.setQuotationId(quotationId);
        acceptCommand.setQuantity(5);
        quotationService.acceptQuotation(acceptCommand);

        // Then
        Quotation quotation = quotationService.getQuotation(quotationId);
        assertEquals("ACCEPTED", quotation.getStatus());
        
        Integer remainingStock = inventoryRepository.selectByProductId(testProductId).getStockQuantity();
        assertEquals(initialStock - 5, remainingStock);
    }

    @Test
    void testConcurrentAcceptQuotation_OptimisticLock() throws InterruptedException {
        // Given: 创建报价单
        CreateQuotationCommand createCommand = new CreateQuotationCommand();
        createCommand.setProductId(testProductId);
        createCommand.setSellerId(testSellerId);
        createCommand.setBuyerId(testBuyerId);
        createCommand.setOriginalPrice(new BigDecimal("100.00"));
        createCommand.setQuotedPrice(new BigDecimal("90.00"));
        createCommand.setExpiredAt(LocalDateTime.now().plusHours(24));
        Long quotationId = quotationService.createQuotation(createCommand);

        // When: 模拟并发接受报价
        final int[] successCount = {0};
        final int[] failCount = {0};
        
        Thread thread1 = new Thread(() -> {
            try {
                AcceptQuotationCommand cmd = new AcceptQuotationCommand();
                cmd.setQuotationId(quotationId);
                cmd.setQuantity(1);
                quotationService.acceptQuotation(cmd);
                successCount[0]++;
            } catch (Exception e) {
                failCount[0]++;
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                AcceptQuotationCommand cmd = new AcceptQuotationCommand();
                cmd.setQuotationId(quotationId);
                cmd.setQuantity(1);
                quotationService.acceptQuotation(cmd);
                successCount[0]++;
            } catch (Exception e) {
                failCount[0]++;
            }
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        // Then: 只有一个能成功，另一个会因状态变更或乐观锁失败
        assertEquals(1, successCount[0], "只有一个线程应该成功");
        assertTrue(failCount[0] >= 1, "至少一个线程应该失败");
        
        // 验证最终状态
        Quotation quotation = quotationService.getQuotation(quotationId);
        assertEquals("ACCEPTED", quotation.getStatus());
    }

    @Test
    void testRejectQuotation() {
        // Given: 创建报价单
        CreateQuotationCommand createCommand = new CreateQuotationCommand();
        createCommand.setProductId(testProductId);
        createCommand.setSellerId(testSellerId);
        createCommand.setBuyerId(testBuyerId);
        createCommand.setOriginalPrice(new BigDecimal("100.00"));
        createCommand.setQuotedPrice(new BigDecimal("90.00"));
        createCommand.setExpiredAt(LocalDateTime.now().plusHours(24));
        Long quotationId = quotationService.createQuotation(createCommand);

        // When: 买方拒绝报价
        RejectQuotationCommand rejectCommand = new RejectQuotationCommand();
        rejectCommand.setQuotationId(quotationId);
        quotationService.rejectQuotation(rejectCommand);

        // Then
        Quotation quotation = quotationService.getQuotation(quotationId);
        assertEquals("REJECTED", quotation.getStatus());
    }
}
