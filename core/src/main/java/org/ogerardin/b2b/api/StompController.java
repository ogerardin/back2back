package org.ogerardin.b2b.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.time.Instant;

@Controller
public class StompController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Scheduled(fixedDelay = 5000)
    public void postToTopic() {
        simpMessagingTemplate.convertAndSend("/topic/message", getMessage());
    }

    @MessageMapping("/hello")
    @SendTo("/topic/message")
    public PushMessage handleHello(PushMessage message) {
        return getMessage();
    }

    private PushMessage getMessage() {
        return new PushMessage() {
            {
                message = "Hello, " + Instant.now() + "!";
            }
        };
    }
}
