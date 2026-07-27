package com.example.quotations;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 报价系统启动类
 */
@SpringBootApplication
@EnableAsync // 启用异步处理（用于事件监听器）
@MapperScan("com.example.quotations.domain.repository")
public class QuotationApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuotationApplication.class, args);
    }
}
