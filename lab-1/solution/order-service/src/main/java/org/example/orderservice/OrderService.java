package org.example.orderservice;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient userServiceWebClient;

    public OrderService(OrderRepository orderRepository, WebClient userServiceWebClient) {
        this.orderRepository = orderRepository;
        this.userServiceWebClient = userServiceWebClient;
    }

    public List<Order> getOrders() {
        return orderRepository.findAll();
    }

    public OrderExtended getOrder(Long orderId) {
        // 1. get Order from repository by orderId
        // 2. get userId from Order
        // 3. send GET request to UserService in order to get User
        // 4. return extended order

        var order = orderRepository.findById(orderId)
                .orElseThrow();
        var userId = order.getUserId();
        var user = getUser(userId);

        return OrderMapper.toExtended(order, user.orElse(null));
    }

    public Order saveOrder(Order order) {
        var userId = order.getUserId();
        var user = getUser(userId);

        if (user.isPresent())
            return orderRepository.save(order);

        return null;
    }

    @Transactional
    public Order updateOrder(Long orderId, Order order) {
        if (orderId.equals(order.getId()) && orderRepository.existsById(orderId))
            return saveOrder(order);

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
