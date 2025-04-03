package org.example.orderservice;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderExtended {
    private Long id;
    private String description;
    private Double sum;
    private LocalDateTime createdAt;
    private UserDTO user;
}
