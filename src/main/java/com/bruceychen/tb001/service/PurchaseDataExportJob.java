package com.bruceychen.tb001.service;

import com.bruceychen.tb001.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PurchaseDataExportJob {

    private static final Logger log = LoggerFactory.getLogger(PurchaseDataExportJob.class);

    private final OrderRepository orderRepository;

    public PurchaseDataExportJob(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Scheduled(cron = "0 */30 * * * *")
    public void exportPurchaseData() {
        long count = orderRepository.count();
        log.info("[purchase-export] would export {} orders to analysis service", count);
    }
}
