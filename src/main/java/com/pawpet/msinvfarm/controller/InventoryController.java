package com.pawpet.msinvfarm.controller;

import com.pawpet.msinvfarm.model.Product;
import com.pawpet.msinvfarm.model.StockHistory;
import com.pawpet.msinvfarm.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Inventory Management", description = "APIs for managing pharmacy inventory, products, and stock")
public class InventoryController {
    
    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);
    
    private final InventoryService inventoryService;
    
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    
    @GetMapping("/products")
    @Operation(summary = "Get all products", description = "Retrieve a paginated list of products with optional filtering by category and active status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved products")
    })
    public ResponseEntity<List<Product>> getProducts(
            @Parameter(description = "Page number (starting from 1)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort order (asc or desc)") @RequestParam(defaultValue = "asc") String sortOrder,
            @Parameter(description = "Filter by category") @RequestParam(required = false) String category,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean isActive) {
        log.info("GET /api/inventory/products - page: {}, limit: {}", page, limit);
        return ResponseEntity.ok(inventoryService.getProducts(page, limit, sortBy, sortOrder, category, isActive).getContent());
    }
    
    @GetMapping("/products/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve a specific product by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product found"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Product> getProductById(@Parameter(description = "Product ID") @PathVariable Long id) {
        log.info("GET /api/inventory/products/{}", id);
        return inventoryService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/products/sku/{sku}")
    @Operation(summary = "Get product by SKU", description = "Retrieve a specific product by its unique SKU. Used by BFF to verify vaccine existence in medical records.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product found"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Product> getProductBySku(@Parameter(description = "Product SKU") @PathVariable String sku) {
        log.info("GET /api/inventory/products/sku/{}", sku);
        return inventoryService.getProductBySku(sku)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/products")
    @Operation(summary = "Create a new product", description = "Create a new product in the inventory")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product created successfully")
    })
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        log.info("POST /api/inventory/products - name: {}", product.getName());
        return ResponseEntity.ok(inventoryService.createProduct(product));
    }
    
    @PutMapping("/products/{id}")
    @Operation(summary = "Update a product", description = "Update an existing product's information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product updated successfully")
    })
    public ResponseEntity<Product> updateProduct(@Parameter(description = "Product ID") @PathVariable Long id, @RequestBody Product product) {
        log.info("PUT /api/inventory/products/{}", id);
        return ResponseEntity.ok(inventoryService.updateProduct(id, product));
    }
    
    @DeleteMapping("/products/{id}")
    @Operation(summary = "Delete a product", description = "Delete a product from the inventory")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Product deleted successfully")
    })
    public ResponseEntity<Void> deleteProduct(@Parameter(description = "Product ID") @PathVariable Long id) {
        log.info("DELETE /api/inventory/products/{}", id);
        inventoryService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/products/{id}/stock")
    @Operation(summary = "Update product stock", description = "Update the stock quantity of a product and record the change in history")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stock updated successfully")
    })
    public ResponseEntity<Product> updateStock(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Parameter(description = "Quantity to add (can be negative)") @RequestParam Integer quantity,
            @Parameter(description = "Type of update (e.g., MANUAL, SALE, RESTOCK)") @RequestParam(defaultValue = "MANUAL") String updateType) {
        log.info("PATCH /api/inventory/products/{}/stock - quantity: {}", id, quantity);
        return ResponseEntity.ok(inventoryService.updateStock(id, quantity, updateType));
    }
    
    @GetMapping("/products/low-stock")
    @Operation(summary = "Get low stock products", description = "Retrieve products with stock at or below minimum threshold")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved low stock products")
    })
    public ResponseEntity<List<Product>> getLowStockProducts() {
        log.info("GET /api/inventory/products/low-stock");
        return ResponseEntity.ok(inventoryService.getLowStockProducts());
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get stock alerts", description = "Retrieve products with low stock for the admin dashboard. Used by BFF to display alerts.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved stock alerts")
    })
    public ResponseEntity<List<Product>> getStockAlerts() {
        log.info("GET /api/inventory/alerts");
        return ResponseEntity.ok(inventoryService.getLowStockProducts());
    }
    
    @GetMapping("/products/{id}/history")
    @Operation(summary = "Get stock history", description = "Retrieve the stock change history for a specific product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved stock history")
    })
    public ResponseEntity<Page<StockHistory>> getStockHistory(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Filter by date from") @RequestParam(required = false) LocalDateTime dateFrom,
            @Parameter(description = "Filter by date to") @RequestParam(required = false) LocalDateTime dateTo) {
        log.info("GET /api/inventory/products/{}/history", id);
        return ResponseEntity.ok(inventoryService.getStockHistory(id, page, limit, dateFrom, dateTo));
    }
    
    @GetMapping("/stats")
    @Operation(summary = "Get inventory statistics", description = "Retrieve overall inventory statistics including counts, values, and categories")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved inventory statistics")
    })
    public ResponseEntity<Map<String, Object>> getInventoryStats() {
        log.info("GET /api/inventory/stats");
        return ResponseEntity.ok(inventoryService.getInventoryStats());
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search products by name, description, or SKU with optional filters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved search results")
    })
    public ResponseEntity<Page<Product>> searchProducts(
            @Parameter(description = "Search query") @RequestParam(required = false) String query,
            @Parameter(description = "Filter by category") @RequestParam(required = false) String category,
            @Parameter(description = "Filter by minimum price") @RequestParam(required = false) Double minPrice,
            @Parameter(description = "Filter by maximum price") @RequestParam(required = false) Double maxPrice,
            @Parameter(description = "Filter by in-stock status") @RequestParam(required = false) Boolean inStock,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/inventory/search - query: {}", query);
        return ResponseEntity.ok(inventoryService.searchProducts(query, category, minPrice, maxPrice, inStock, page, limit));
    }
    
    @GetMapping("/categories")
    @Operation(summary = "Get categories", description = "Retrieve all product categories with their product counts")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved categories")
    })
    public ResponseEntity<List<Map<String, Object>>> getCategories() {
        log.info("GET /api/inventory/categories");
        return ResponseEntity.ok(inventoryService.getCategories());
    }
    
    @GetMapping("/category/{category}")
    @Operation(summary = "Get products by category", description = "Retrieve all products belonging to a specific category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved products")
    })
    public ResponseEntity<Page<Product>> getProductsByCategory(
            @Parameter(description = "Category name") @PathVariable String category,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort order (asc or desc)") @RequestParam(defaultValue = "asc") String sortOrder) {
        log.info("GET /api/inventory/category/{}", category);
        return ResponseEntity.ok(inventoryService.getProductsByCategory(category, page, limit, sortBy, sortOrder));
    }
    
    @GetMapping("/location/{location}")
    @Operation(summary = "Get products by location", description = "Retrieve all products stored at a specific location")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved products")
    })
    public ResponseEntity<List<Product>> getProductsByLocation(@Parameter(description = "Storage location") @PathVariable String location) {
        log.info("GET /api/inventory/location/{}", location);
        return ResponseEntity.ok(inventoryService.getProductsByLocation(location));
    }
    
    @GetMapping("/valuation")
    @Operation(summary = "Get product valuation", description = "Calculate the total value of inventory including cost and selling price")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved valuation")
    })
    public ResponseEntity<Map<String, Object>> getProductValuation() {
        log.info("GET /api/inventory/valuation");
        return ResponseEntity.ok(inventoryService.getProductValuation());
    }
    
    @PatchMapping("/products/{id}/thresholds")
    @Operation(summary = "Update product thresholds", description = "Update the minimum and maximum stock thresholds for a product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Thresholds updated successfully")
    })
    public ResponseEntity<Product> updateProductThresholds(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Parameter(description = "Minimum stock threshold") @RequestParam Integer minStock,
            @Parameter(description = "Maximum stock threshold") @RequestParam(required = false) Integer maxStock) {
        log.info("PATCH /api/inventory/products/{}/thresholds", id);
        return ResponseEntity.ok(inventoryService.updateProductThresholds(id, minStock, maxStock));
    }
}
