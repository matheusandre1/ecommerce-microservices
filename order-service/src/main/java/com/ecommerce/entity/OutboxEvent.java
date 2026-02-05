package com.ecommerce.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox")
@SequenceGenerator(
        name = "outbox_seq_gen",
        sequenceName = "outbox_seq",
        allocationSize = 50
)
public class OutboxEvent extends PanacheEntity {

    @NotNull
    public String aggregateType;

    @NotNull
    public String aggregateId;

    @NotNull
    public String eventType;

    @Column(columnDefinition = "TEXT")
    public String payload;

    @Column(updatable = false)
    public LocalDateTime createdAt;

    public OutboxEvent() {
        this.createdAt = LocalDateTime.now();
    }

    public OutboxEvent(String aggregateType, String aggregateId, String eventType, String payload) {
        this();
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
    }
}
