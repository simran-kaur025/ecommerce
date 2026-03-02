package com.bootcamp.ecommerce.controller;
import com.bootcamp.ecommerce.producer.MessageProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rabbit")
@RequiredArgsConstructor
public class RabbitController {

    private final MessageProducer producer;

    @PostMapping("/send")
    public String send(@RequestParam String msg) {
        producer.sendMessage(msg);
        return "Message sent";
    }
}
