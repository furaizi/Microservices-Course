package org.example.orderservice;

public class OrderMapper {
    public static OrderExtended toExtended(Order order, UserDTO user) {
        if (order == null)
            return null;

        return OrderExtended.builder()
                .id(order.getId())
                .description(order.getDescription())
                .sum(order.getSum())
                .createdAt(order.getCreatedAt())
                .user(user)
                .build();
    }
}
