package com.ecommerce.consumer;

import com.ecommerce.event.OrderCreatedEvent;
import com.ecommerce.event.OrderStatusChangedEvent;
import com.ecommerce.service.NotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class OrderEventConsumer {

    private static final Logger LOG = Logger.getLogger(OrderEventConsumer.class);

    @Inject
    NotificationService notificationService;

    @Inject
    ObjectMapper objectMapper;

    @Incoming("order-events")
    @Blocking
    public void onOrderEvent(String message) {
        try {
            LOG.debugf("[KAFKA] Received raw message from outbox.event.Order: %s", message);

            JsonNode jsonNode = objectMapper.readTree(message);

            if (jsonNode.isTextual()) {
                String innerJson = jsonNode.asText();
                jsonNode = objectMapper.readTree(innerJson);
                LOG.debugf("[KAFKA] Detected double-encoded JSON, parsed inner content");
            }

            if (jsonNode.has("oldStatus") && jsonNode.has("newStatus")) {
                OrderStatusChangedEvent event = objectMapper.treeToValue(jsonNode, OrderStatusChangedEvent.class);
                LOG.infof("[KAFKA] Successfully parsed OrderStatusChangedEvent: orderId=%d", event.orderId());
                handleOrderStatusChanged(event);
            } else if (jsonNode.has("items") && jsonNode.has("customerName")) {
                OrderCreatedEvent event = objectMapper.treeToValue(jsonNode, OrderCreatedEvent.class);
                LOG.infof("[KAFKA] Successfully parsed OrderCreatedEvent: orderId=%d", event.orderId());
                handleOrderCreated(event);
            } else {
                LOG.warnf("[KAFKA] Unknown event type in outbox.event.Order - payload: %s", message);
            }

        } catch (Exception e) {
            LOG.errorf(e, "[KAFKA] Failed to process outbox.event.Order message: %s - error: %s", message, e.getMessage());
        }
    }

    private void handleOrderCreated(OrderCreatedEvent event) {
        try {
            LOG.infof("[KAFKA] Processing OrderCreated event: orderId=%d, customer=%s, total=R$%.2f",
                    event.orderId(), event.customerName(), event.totalAmount());

            notificationService.notifyOrderCreated(
                    event.orderId(),
                    event.customerEmail(),
                    event.customerName(),
                    event.totalAmount()
            );

            LOG.infof("[KAFKA] OrderCreated event processed successfully: orderId=%d", event.orderId());
        } catch (Exception e) {
            LOG.errorf(e, "[KAFKA] Failed to process OrderCreated event: orderId=%d", event.orderId());
        }
    }

    private void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        try {
            LOG.infof("[KAFKA] Processing OrderStatusChanged event: orderId=%d, %s → %s",
                    event.orderId(), event.oldStatus(), event.newStatus());

            notificationService.notifyOrderStatusChanged(
                    event.orderId(),
                    event.customerEmail(),
                    event.oldStatus(),
                    event.newStatus()
            );

            LOG.infof("[KAFKA] OrderStatusChanged event processed successfully: orderId=%d", event.orderId());
        } catch (Exception e) {
            LOG.errorf(e, "[KAFKA] Failed to process OrderStatusChanged event: orderId=%d", event.orderId());
        }
    }
}
