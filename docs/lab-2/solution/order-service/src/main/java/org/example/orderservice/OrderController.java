package org.example.orderservice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderExtended> getOrderById(@PathVariable Long id) {
        final var extendedOrder = orderService.getOrder(id);
        if (extendedOrder == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(extendedOrder);
    }

    @PostMapping
    public ResponseEntity<Order> addOrder(@RequestBody Order order) {
        final var addedOrder = orderService.createOrder(order);
        if (addedOrder == null)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addedOrder);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id,
                                             @RequestBody Order order) {
        final var updatedOrder = orderService.updateOrder(id, order);
        if (updatedOrder == null)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
