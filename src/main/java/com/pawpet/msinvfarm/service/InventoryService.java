package com.pawpet.msinvfarm.service;

import com.pawpet.msinvfarm.model.Product;
import com.pawpet.msinvfarm.model.StockHistory;
import com.pawpet.msinvfarm.repository.ProductRepository;
import com.pawpet.msinvfarm.repository.StockHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class InventoryService {
    
    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    
    private final ProductRepository productRepository;
    private final StockHistoryRepository stockHistoryRepository;
    
    public InventoryService(ProductRepository productRepository, StockHistoryRepository stockHistoryRepository) {
        this.productRepository = productRepository;
        this.stockHistoryRepository = stockHistoryRepository;
    }
    
    public Page<Product> getProducts(int page, int limit, String sortBy, String sortOrder, 
                                      String category, Boolean isActive) {
        log.info("Retrieving products - page: {}, limit: {}", page, limit);
        
        Sort sort = Sort.by(sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page - 1, limit, sort);
        
        if (category != null && isActive != null) {
            return productRepository.findByCategoryAndIsActive(category, isActive, pageable);
        } else if (category != null) {
            return productRepository.findByCategory(category, pageable);
        } else if (isActive != null) {
            return productRepository.findByIsActive(isActive, pageable);
        }
        
        return productRepository.findAll(pageable);
    }
    
    public Optional<Product> getProductById(Long id) {
        log.info("Retrieving product by ID: {}", id);
        return productRepository.findById(id);
    }
    
    public Optional<Product> getProductBySku(String sku) {
        log.info("Retrieving product by SKU: {}", sku);
        return productRepository.findBySku(sku);
    }
    
    @Transactional
    public Product createProduct(Product productData) {
        log.info("Creating product: {}", productData.getName());
        
        if (productData.getName() == null || productData.getSku() == null || productData.getPrice() == null) {
            throw new IllegalArgumentException("Name, SKU, and price are required");
        }
        
        Product product = Product.builder()
                .sku(productData.getSku())
                .name(productData.getName())
                .description(productData.getDescription())
                .category(productData.getCategory())
                .price(productData.getPrice())
                .cost(productData.getCost())
                .stock(productData.getStock() != null ? productData.getStock() : 0)
                .minStock(productData.getMinStock() != null ? productData.getMinStock() : 5)
                .maxStock(productData.getMaxStock())
                .location(productData.getLocation())
                .supplier(productData.getSupplier())
                .isActive(productData.getIsActive() != null ? productData.getIsActive() : true)
                .build();
        
        Product savedProduct = productRepository.save(product);
        checkLowStock(savedProduct);
        
        log.info("Product created: {} (ID: {})", savedProduct.getName(), savedProduct.getId());
        return savedProduct;
    }
    
    @Transactional
    public Product updateProduct(Long id, Product updateData) {
        log.info("Updating product ID: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        
        Integer oldStock = product.getStock();
        
        if (updateData.getName() != null) product.setName(updateData.getName());
        if (updateData.getDescription() != null) product.setDescription(updateData.getDescription());
        if (updateData.getCategory() != null) product.setCategory(updateData.getCategory());
        if (updateData.getPrice() != null) product.setPrice(updateData.getPrice());
        if (updateData.getCost() != null) product.setCost(updateData.getCost());
        if (updateData.getStock() != null) {
            product.setStock(updateData.getStock());
            if (updateData.getStock() != oldStock) {
                recordStockChange(id, oldStock, updateData.getStock(), "MANUAL");
            }
        }
        if (updateData.getMinStock() != null) product.setMinStock(updateData.getMinStock());
        if (updateData.getMaxStock() != null) product.setMaxStock(updateData.getMaxStock());
        if (updateData.getLocation() != null) product.setLocation(updateData.getLocation());
        if (updateData.getSupplier() != null) product.setSupplier(updateData.getSupplier());
        if (updateData.getIsActive() != null) product.setIsActive(updateData.getIsActive());
        
        Product updatedProduct = productRepository.save(product);
        checkLowStock(updatedProduct);
        
        log.info("Product updated: {} (ID: {})", updatedProduct.getName(), id);
        return updatedProduct;
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product ID: {}", id);
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found");
        }
        productRepository.deleteById(id);
        log.info("Product deleted: ID {}", id);
    }
    
    @Transactional
    public Product updateStock(Long id, Integer quantity, String updateType) {
        log.info("Updating stock for product ID: {}, quantity: {}, type: {}", id, quantity, updateType);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        
        Integer oldStock = product.getStock();
        Integer newStock = Math.max(0, oldStock + quantity);
        
        product.setStock(newStock);
        Product updatedProduct = productRepository.save(product);
        
        recordStockChange(id, oldStock, newStock, updateType);
        checkLowStock(updatedProduct);
        
        log.info("Stock updated for product {}: {} -> {} ({})", product.getName(), oldStock, newStock, updateType);
        return updatedProduct;
    }
    
    public List<Product> getLowStockProducts() {
        log.info("Retrieving low stock products");
        List<Product> products = productRepository.findLowStockProducts();
        products.sort((a, b) -> a.getStock() - b.getStock());
        return products;
    }
    
    public Page<StockHistory> getStockHistory(Long productId, int page, int limit, 
                                             LocalDateTime dateFrom, LocalDateTime dateTo) {
        log.info("Retrieving stock history for product ID: {}", productId);
        Pageable pageable = PageRequest.of(page - 1, limit);
        
        if (dateFrom != null || dateTo != null) {
            return stockHistoryRepository.findByProductIdWithDateRange(productId, dateFrom, dateTo, pageable);
        }
        return stockHistoryRepository.findByProductIdOrderByTimestampDesc(productId, pageable);
    }
    
    public Map<String, Object> getInventoryStats() {
        log.info("Retrieving inventory statistics");
        
        Long totalProducts = productRepository.countAll();
        Long activeProducts = productRepository.countActive();
        List<Product> lowStockProducts = productRepository.findLowStockProducts();
        List<Product> outOfStockProducts = productRepository.findOutOfStockProducts();
        
        List<Product> allProducts = productRepository.findAll();
        Double totalValue = allProducts.stream()
                .mapToDouble(p -> p.getStock() * p.getPrice())
                .sum();
        
        List<String> categories = productRepository.findAllCategories();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", totalProducts);
        stats.put("activeProducts", activeProducts);
        stats.put("inactiveProducts", totalProducts - activeProducts);
        stats.put("lowStockProducts", lowStockProducts.size());
        stats.put("outOfStockProducts", outOfStockProducts.size());
        stats.put("totalValue", totalValue);
        stats.put("categories", categories.size());
        stats.put("timestamp", LocalDateTime.now().toString());
        
        return stats;
    }
    
    public Page<Product> searchProducts(String query, String category, Double minPrice, Double maxPrice, 
                                        Boolean inStock, int page, int limit) {
        log.info("Searching products with query: {}", query);
        Pageable pageable = PageRequest.of(page - 1, limit);
        
        if (query != null && !query.isEmpty()) {
            return productRepository.search(query, pageable);
        }
        return productRepository.findAll(pageable);
    }
    
    public List<Map<String, Object>> getCategories() {
        log.info("Retrieving categories");
        List<String> categoryNames = productRepository.findAllCategories();
        List<Product> allProducts = productRepository.findAll();
        
        return categoryNames.stream()
                .map(category -> {
                    Map<String, Object> categoryData = new HashMap<>();
                    categoryData.put("name", category);
                    categoryData.put("count", allProducts.stream()
                            .filter(p -> category.equals(p.getCategory()))
                            .count());
                    return categoryData;
                })
                .toList();
    }
    
    public Page<Product> getProductsByCategory(String category, int page, int limit, String sortBy, String sortOrder) {
        log.info("Retrieving products by category: {}", category);
        Sort sort = Sort.by(sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page - 1, limit, sort);
        
        return productRepository.findByCategory(category, pageable);
    }
    
    public List<Product> getProductsByLocation(String location) {
        log.info("Retrieving products by location: {}", location);
        return productRepository.findByLocation(location);
    }
    
    public Map<String, Object> getProductValuation() {
        log.info("Calculating product valuation");
        List<Product> allProducts = productRepository.findAll();
        Double totalValue = allProducts.stream()
                .mapToDouble(p -> p.getStock() * p.getPrice())
                .sum();
        Double stockValue = allProducts.stream()
                .mapToDouble(p -> p.getStock() * (p.getCost() != null ? p.getCost() : 0.0))
                .sum();
        
        Map<String, Object> valuation = new HashMap<>();
        valuation.put("totalValue", totalValue);
        valuation.put("stockValue", stockValue);
        valuation.put("profitValue", totalValue - stockValue);
        valuation.put("productCount", allProducts.size());
        valuation.put("timestamp", LocalDateTime.now().toString());
        
        return valuation;
    }
    
    @Transactional
    public Product updateProductThresholds(Long id, Integer minStock, Integer maxStock) {
        log.info("Updating product thresholds ID: {}, min: {}, max: {}", id, minStock, maxStock);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        
        product.setMinStock(minStock);
        product.setMaxStock(maxStock);
        Product updatedProduct = productRepository.save(product);
        checkLowStock(updatedProduct);
        
        log.info("Product thresholds updated: {} (min: {}, max: {})", product.getName(), minStock, maxStock);
        return updatedProduct;
    }
    
    private void checkLowStock(Product product) {
        if (product.getStock() <= product.getMinStock()) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastAlert = product.getLastStockAlert();
            
            boolean shouldAlert = lastAlert == null || 
                    lastAlert.isBefore(now.minusHours(1)) || 
                    product.getStock() == 0;
            
            if (shouldAlert) {
                product.setLastStockAlert(now);
                productRepository.save(product);
                log.warn("Low stock alert triggered for product {}: {} units (threshold: {})", 
                        product.getName(), product.getStock(), product.getMinStock());
            }
        }
    }
    
    private void recordStockChange(Long productId, Integer oldStock, Integer newStock, String updateType) {
        StockHistory history = StockHistory.builder()
                .productId(productId)
                .oldStock(oldStock)
                .newStock(newStock)
                .change(newStock - oldStock)
                .updateType(updateType)
                .build();
        
        stockHistoryRepository.save(history);
        log.info("Stock change recorded for product {}: {} -> {} ({})", productId, oldStock, newStock, updateType);
    }
}