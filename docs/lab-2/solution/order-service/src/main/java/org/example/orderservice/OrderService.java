package org.example.orderservice;

import org.example.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final WebClient userServiceWebClient;
    private final KafkaTemplate<Long, Object> kafkaTemplate;

    public OrderService(OrderRepository orderRepository, WebClient userServiceWebClient, KafkaTemplate<Long, Object> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.userServiceWebClient = userServiceWebClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Value("${app.kafka.order-created-topic}")
    private String orderCreatedTopic;

    public List<Order> getOrders() {
        return orderRepository.findAll();
    }

    public OrderExtended getOrder(Long orderId) {
        var order = orderRepository.findById(orderId)
                .orElseThrow();
        var userId = order.getUserId();
        var user = getUser(userId);

        return OrderMapper.toExtended(order, user.orElse(null));
    }

    public Order createOrder(Order order) {
        var userId = order.getUserId();
        var user = getUser(userId);

        if (user.isPresent()) {
            var savedOrder = orderRepository.save(order);
            log.info("Order created successfully with ID: {}", savedOrder.getId());
            var event = new OrderCreatedEvent(
                    savedOrder.getId(),
                    savedOrder.getUserId(),
                    LocalDateTime.now()
            );

            try {
                CompletableFuture<SendResult<Long, Object>> future = kafkaTemplate.send(orderCreatedTopic, savedOrder.getId(), event);
                future.whenComplete((result, ex) -> {
                    if (ex == null)
                        log.info("Sent event [{}] to topic [{}], partition [{}], offset [{}]",
                                event,
                                orderCreatedTopic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    else
                        log.error("Failed to send event [{}] to topic [{}]: {}",
                                event, orderCreatedTopic, ex.getMessage(), ex);
                });
            }
            catch (Exception e) {
                log.error("Exception during sending event to Kafka: {}", e.getMessage(), e);
            }

            return savedOrder;
        }

        return null;
    }

    @Transactional
    public Order updateOrder(Long orderId, Order order) {
        if (orderId.equals(order.getId()) && orderRepository.existsById(orderId))
            return orderRepository.save(order);

        return null;
    }

    public void deleteOrder(Long orderId) {
        orderRepository.deleteById(orderId);
    }

    private Optional<UserDTO> getUser(Long userId) {
        return userServiceWebClient
                .get()
                .uri("/{userId}", userId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.empty())
                .bodyToMono(UserDTO.class)
                .onErrorResume(error -> Mono.empty())
                .blockOptional();
    }
}
