package com.pawpet.msinvfarm.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    private String description;
    private String category;

    @Column(nullable = false)
    private Double price;

    private Double cost;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(name = "min_stock", nullable = false)
    private Integer minStock = 5;

    @Column(name = "max_stock")
    private Integer maxStock;

    private String location;
    private String supplier;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "last_stock_alert")
    private LocalDateTime lastStockAlert;
}