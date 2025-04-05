package org.example.notificationservice;

import org.example.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final WebClient userServiceWebClient;

    public NotificationService(WebClient userServiceWebClient) {
        this.userServiceWebClient = userServiceWebClient;
    }

    @KafkaListener(topics = "order_created_topic", groupId = "notification_group")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Received order created event: {}", event);
        if (event == null || event.getUserId() == null) {
            log.error("Received invalid event data: {}", event);
            return;
        }

        try {
            var user = getUserInfo(event.getUserId());
            if (user.getEmail() == null) {
                log.warn("Could not retrieve email for userId: {}", event.getUserId());
                return;
            }
            var notification = createNotification(event.getOrderId(), user);
            sendNotification(user.getEmail(), notification);
        } catch (Exception e) {
            log.error("Error processing order created event for orderId {}: {}", event.getOrderId(), e.getMessage(), e);
        }
    }

    private User getUserInfo(Long userId) {
        return userServiceWebClient
                .get()
                .uri("/{userId}", userId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.empty())
                .bodyToMono(User.class)
                .onErrorResume(error -> Mono.empty())
                .block();
    }

    private String createNotification(Long orderId, User user) {
        return String.format("Dear user %s, your order with ID %d is created successfully.", user.getName(), orderId);
    }

    private void sendNotification(String email, String notification) {
        log.info("[NotificationService] Simulating sending EMAIL to [{}]. Content: \"{}\"",
                email, notification);
    }

}
