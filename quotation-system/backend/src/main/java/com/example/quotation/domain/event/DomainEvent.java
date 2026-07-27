package com.example.quotation.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 领域事件基类
 */
@Data
@AllArgsConstructor
public abstract class DomainEvent {
    private LocalDateTime occurredOn;
}
