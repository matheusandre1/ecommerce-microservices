package com.ecommerce.consumer;

import com.ecommerce.event.OrderCancelledEvent;
import com.ecommerce.event.OrderCreatedEvent;
import com.ecommerce.service.ProductService;
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
    ProductService productService;

    @Inject
    ObjectMapper objectMapper;

    @Incoming("order-events")
    @Blocking
    public void onOrderEvent(String message) {
        try {
            LOG.debugf("[KAFKA] Received message from Order.events: %s", message);

            JsonNode jsonNode = objectMapper.readTree(message);

            if (jsonNode.isTextual()) {
                String innerJson = jsonNode.asText();
                jsonNode = objectMapper.readTree(innerJson);
                LOG.debugf("[KAFKA] Detected double-encoded JSON, parsed inner content");
            }

            if (jsonNode.has("items") && jsonNode.has("customerName")) {
                if (jsonNode.has("cancelledAt")) {
                    OrderCancelledEvent event = objectMapper.treeToValue(jsonNode, OrderCancelledEvent.class);
                    handleOrderCancelled(event);
                }else {
                    OrderCreatedEvent event = objectMapper.treeToValue(jsonNode, OrderCreatedEvent.class);
                    handleOrderCreated(event);
                }
            } else {
                LOG.debugf("[KAFKA] Ignoring non-OrderCreated event from Order.events");
            }

        } catch (Exception e) {
            LOG.errorf(e, "[KAFKA] Failed to process Order.events message: %s", message);
        }
    }

    private void handleOrderCreated(OrderCreatedEvent event) {
        try {
            LOG.infof("[KAFKA] Processing OrderCreated event: orderId=%d, customer=%s, total=R$%.2f",
                    event.orderId(), event.customerName(), event.totalAmount());

            for (var item : event.items()) {
                try {
                    productService.decreaseStock(item.productId(), item.quantity());
                    LOG.infof("[KAFKA] Stock decreased for product %s: -%d", item.productId(), item.quantity());
                } catch (Exception e) {
                    LOG.errorf(e, "[KAFKA] Failed to decrease stock for product %s in order %d", item.productId(), event.orderId());
                }
            }

            LOG.infof("[KAFKA] OrderCreated event processed successfully: orderId=%d", event.orderId());
        } catch (Exception e) {
            LOG.errorf(e, "[KAFKA] Failed to process OrderCreated event: orderId=%d", event.orderId());
        }
    }


    private void handleOrderCancelled(OrderCancelledEvent event) {
        try {
            LOG.infof("[KAFKA] Processing OrderCancelled event: orderId=%d, customer=%s, total=R$%.2f",
                    event.orderId(), event.customerName(), event.totalAmount());

            for (var item : event.items()) {
                try {
                    productService.increaseStock(item.productId(), item.quantity());
                    LOG.infof("[KAFKA] Stock increased for product %s: +%d", item.productId(), item.quantity());
                } catch (Exception e) {
                    LOG.errorf(e, "[KAFKA] Failed to increase stock for product %s in cancelled order %d", item.productId(), event.orderId());
                }
            }

            LOG.infof("[KAFKA] OrderCancelled event processed successfully: orderId=%d", event.orderId());
        } catch (Exception e) {
            LOG.errorf(e, "[KAFKA] Failed to process OrderCancelled event: orderId=%d", event.orderId());
        }
    }
}
