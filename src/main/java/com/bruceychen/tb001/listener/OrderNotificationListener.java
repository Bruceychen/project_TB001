package com.bruceychen.tb001.listener;

import com.bruceychen.tb001.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OrderNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationListener.class);

    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("[notification] would send email to user {} ({}): order #{} confirmed, totalCost = {}",
                event.username(), event.userId(), event.orderId(), event.totalCost());
    }
}
