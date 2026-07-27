package com.example.quotations.domain.event;

import lombok.Getter;
import java.time.LocalDateTime;

/**
 * 领域事件基类
 */
@Getter
public abstract class DomainEvent {
    private final LocalDateTime occurredOn;

    protected DomainEvent() {
        this.occurredOn = LocalDateTime.now();
    }
}
