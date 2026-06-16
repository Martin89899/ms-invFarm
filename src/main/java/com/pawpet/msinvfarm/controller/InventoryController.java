package com.pawpet.msinvfarm.controller;

import com.pawpet.msinvfarm.model.Product;
import com.pawpet.msinvfarm.model.StockHistory;
import com.pawpet.msinvfarm.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    
    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);
    
    private final InventoryService inventoryService;
    
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    
    @GetMapping("/products")
    public ResponseEntity<Page<Product>> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean isActive) {
        log.info("GET /api/inventory/products - page: {}, limit: {}", page, limit);
        return ResponseEntity.ok(inventoryService.getProducts(page, limit, sortBy, sortOrder, category, isActive));
    }
    
    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        log.info("GET /api/inventory/products/{}", id);
        return inventoryService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/products/sku/{sku}")
    public ResponseEntity<Product> getProductBySku(@PathVariable String sku) {
        log.info("GET /api/inventory/products/sku/{}", sku);
        return inventoryService.getProductBySku(sku)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        log.info("POST /api/inventory/products - name: {}", product.getName());
        return ResponseEntity.ok(inventoryService.createProduct(product));
    }
    
    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        log.info("PUT /api/inventory/products/{}", id);
        return ResponseEntity.ok(inventoryService.updateProduct(id, product));
    }
    
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("DELETE /api/inventory/products/{}", id);
        inventoryService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/products/{id}/stock")
    public ResponseEntity<Product> updateStock(
            @PathVariable Long id,
            @RequestParam Integer quantity,
            @RequestParam(defaultValue = "MANUAL") String updateType) {
        log.info("PATCH /api/inventory/products/{}/stock - quantity: {}", id, quantity);
        return ResponseEntity.ok(inventoryService.updateStock(id, quantity, updateType));
    }
    
    @GetMapping("/products/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts() {
        log.info("GET /api/inventory/products/low-stock");
        return ResponseEntity.ok(inventoryService.getLowStockProducts());
    }
    
    @GetMapping("/products/{id}/history")
    public ResponseEntity<Page<StockHistory>> getStockHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) LocalDateTime dateFrom,
            @RequestParam(required = false) LocalDateTime dateTo) {
        log.info("GET /api/inventory/products/{}/history", id);
        return ResponseEntity.ok(inventoryService.getStockHistory(id, page, limit, dateFrom, dateTo));
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getInventoryStats() {
        log.info("GET /api/inventory/stats");
        return ResponseEntity.ok(inventoryService.getInventoryStats());
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<Product>> searchProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/inventory/search - query: {}", query);
        return ResponseEntity.ok(inventoryService.searchProducts(query, category, minPrice, maxPrice, inStock, page, limit));
    }
    
    @GetMapping("/categories")
    public ResponseEntity<List<Map<String, Object>>> getCategories() {
        log.info("GET /api/inventory/categories");
        return ResponseEntity.ok(inventoryService.getCategories());
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<Product>> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {
        log.info("GET /api/inventory/category/{}", category);
        return ResponseEntity.ok(inventoryService.getProductsByCategory(category, page, limit, sortBy, sortOrder));
    }
    
    @GetMapping("/location/{location}")
    public ResponseEntity<List<Product>> getProductsByLocation(@PathVariable String location) {
        log.info("GET /api/inventory/location/{}", location);
        return ResponseEntity.ok(inventoryService.getProductsByLocation(location));
    }
    
    @GetMapping("/valuation")
    public ResponseEntity<Map<String, Object>> getProductValuation() {
        log.info("GET /api/inventory/valuation");
        return ResponseEntity.ok(inventoryService.getProductValuation());
    }
    
    @PatchMapping("/products/{id}/thresholds")
    public ResponseEntity<Product> updateProductThresholds(
            @PathVariable Long id,
            @RequestParam Integer minStock,
            @RequestParam(required = false) Integer maxStock) {
        log.info("PATCH /api/inventory/products/{}/thresholds", id);
        return ResponseEntity.ok(inventoryService.updateProductThresholds(id, minStock, maxStock));
    }
}
