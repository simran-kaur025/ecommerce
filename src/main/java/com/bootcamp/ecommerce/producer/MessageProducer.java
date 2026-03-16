package com.bootcamp.ecommerce.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageProducer {

    @Value("${rabbitmq.queue.name}")
    private String queue;
    @Value("${rabbitmq.exchnage.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routing_key;

    private final RabbitTemplate rabbitTemplate;

    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend(exchange,routing_key, message);
    }
}
