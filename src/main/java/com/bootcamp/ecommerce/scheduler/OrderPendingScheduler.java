package com.bootcamp.ecommerce.scheduler;

import com.bootcamp.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderPendingScheduler {

    private final OrderService orderService;

    @Scheduled(cron = "${scheduler.pending-orders.cron}")
    public void run() {
        orderService.notifyPendingOrders();
    }
}