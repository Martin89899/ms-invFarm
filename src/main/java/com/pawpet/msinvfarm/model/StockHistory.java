package com.pawpet.msinvfarm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_history")
public class StockHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "old_stock", nullable = false)
    private Integer oldStock;
    
    @Column(name = "new_stock", nullable = false)
    private Integer newStock;
    
    @Column(nullable = false)
    private Integer change;
    
    @Column(name = "update_type", nullable = false)
    private String updateType;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
    
    // --- CONSTRUCTORES ---
    
    // Constructor vacío (Obligatorio para JPA / Reemplaza @NoArgsConstructor)
    public StockHistory() {}
    
    // Constructor completo (Reemplaza @AllArgsConstructor)
    public StockHistory(Long id, Long productId, Integer oldStock, Integer newStock, Integer change, String updateType, LocalDateTime timestamp) {
        this.id = id;
        this.productId = productId;
        this.oldStock = oldStock;
        this.newStock = newStock;
        this.change = change;
        this.updateType = updateType;
        this.timestamp = timestamp;
    }

    // --- EVENTOS DE AUDITORÍA ---
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }

    // --- GETTERS Y SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public Integer getOldStock() { return oldStock; }
    public void setOldStock(Integer oldStock) { this.oldStock = oldStock; }
    
    public Integer getNewStock() { return newStock; }
    public void setNewStock(Integer newStock) { this.newStock = newStock; }
    
    public Integer getChange() { return change; }
    public void setChange(Integer change) { this.change = change; }
    
    public String getUpdateType() { return updateType; }
    public void setUpdateType(String updateType) { this.updateType = updateType; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    // --- BUILDER PATTERN (Reemplaza al @Builder de Lombok) ---
    public static StockHistoryBuilder builder() {
        return new StockHistoryBuilder();
    }

    public static class StockHistoryBuilder {
        private Long id;
        private Long productId;
        private Integer oldStock;
        private Integer newStock;
        private Integer change;
        private String updateType;
        private LocalDateTime timestamp;

        StockHistoryBuilder() {}

        public StockHistoryBuilder id(Long id) { this.id = id; return this; }
        public StockHistoryBuilder productId(Long productId) { this.productId = productId; return this; }
        public StockHistoryBuilder oldStock(Integer oldStock) { this.oldStock = oldStock; return this; }
        public StockHistoryBuilder newStock(Integer newStock) { this.newStock = newStock; return this; }
        public StockHistoryBuilder change(Integer change) { this.change = change; return this; }
        public StockHistoryBuilder updateType(String updateType) { this.updateType = updateType; return this; }
        public StockHistoryBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }

        public StockHistory build() {
            return new StockHistory(id, productId, oldStock, newStock, change, updateType, timestamp);
        }
    }
}