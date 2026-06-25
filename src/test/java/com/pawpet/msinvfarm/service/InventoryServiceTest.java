package com.pawpet.msinvfarm.service;

import com.pawpet.msinvfarm.model.Product;
import com.pawpet.msinvfarm.model.StockHistory;
import com.pawpet.msinvfarm.repository.ProductRepository;
import com.pawpet.msinvfarm.repository.StockHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockHistoryRepository stockHistoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .sku("SKU001")
                .name("Test Product")
                .description("Test Description")
                .category("Medicine")
                .price(10.0)
                .cost(5.0)
                .stock(10)
                .minStock(5)
                .maxStock(100)
                .location("A1")
                .supplier("Test Supplier")
                .isActive(true)
                .build();
    }

    @Test
    void getProducts_ShouldReturnPageOfProducts() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        Page<Product> expectedPage = new PageImpl<>(List.of(testProduct));
        
        when(productRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<Product> result = inventoryService.getProducts(1, 10, "name", "asc", null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Product", result.getContent().get(0).getName());
        verify(productRepository, times(1)).findAll(pageable);
    }

    @Test
    void getProductById_ShouldReturnProductWhenExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Optional<Product> result = inventoryService.getProductById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Product", result.get().getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductById_ShouldReturnEmptyWhenNotExists() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Product> result = inventoryService.getProductById(999L);

        assertFalse(result.isPresent());
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void getProductBySku_ShouldReturnProductWhenExists() {
        when(productRepository.findBySku("SKU001")).thenReturn(Optional.of(testProduct));

        Optional<Product> result = inventoryService.getProductBySku("SKU001");

        assertTrue(result.isPresent());
        assertEquals("SKU001", result.get().getSku());
        verify(productRepository, times(1)).findBySku("SKU001");
    }

    @Test
    void getProductBySku_ShouldReturnEmptyWhenNotExists() {
        when(productRepository.findBySku("NONEXISTENT")).thenReturn(Optional.empty());

        Optional<Product> result = inventoryService.getProductBySku("NONEXISTENT");

        assertFalse(result.isPresent());
        verify(productRepository, times(1)).findBySku("NONEXISTENT");
    }

    @Test
    void createProduct_ShouldCreateAndReturnProduct() {
        Product newProduct = Product.builder()
                .sku("SKU002")
                .name("New Product")
                .price(20.0)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = inventoryService.createProduct(newProduct);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_ShouldThrowExceptionWhenRequiredFieldsMissing() {
        Product invalidProduct = Product.builder()
                .name("Invalid Product")
                .build();

        assertThrows(IllegalArgumentException.class, () -> inventoryService.createProduct(invalidProduct));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_ShouldUpdateAndReturnProduct() {
        Product updateData = Product.builder()
                .name("Updated Product")
                .price(15.0)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = inventoryService.updateProduct(1L, updateData);

        assertNotNull(result);
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_ShouldThrowExceptionWhenProductNotFound() {
        Product updateData = Product.builder()
                .name("Updated Product")
                .build();

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> inventoryService.updateProduct(999L, updateData));
        verify(productRepository, times(1)).findById(999L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_ShouldDeleteProductWhenExists() {
        when(productRepository.existsById(1L)).thenReturn(true);

        inventoryService.deleteProduct(1L);

        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteProduct_ShouldThrowExceptionWhenProductNotFound() {
        when(productRepository.existsById(999L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> inventoryService.deleteProduct(999L));
        verify(productRepository, times(1)).existsById(999L);
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    void updateStock_ShouldUpdateStockAndRecordHistory() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = inventoryService.updateStock(1L, 5, "SALE");

        assertNotNull(result);
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
        verify(stockHistoryRepository, times(1)).save(any(StockHistory.class));
    }

    @Test
    void updateStock_ShouldThrowExceptionWhenProductNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> inventoryService.updateStock(999L, 5, "SALE"));
        verify(productRepository, times(1)).findById(999L);
        verify(productRepository, never()).save(any(Product.class));
        verify(stockHistoryRepository, never()).save(any(StockHistory.class));
    }

    @Test
    void getLowStockProducts_ShouldReturnLowStockProducts() {
        Product lowStockProduct = Product.builder()
                .id(2L)
                .sku("SKU002")
                .name("Low Stock Product")
                .stock(3)
                .minStock(5)
                .isActive(true)
                .build();

        List<Product> lowStockProducts = Arrays.asList(lowStockProduct, testProduct);
        when(productRepository.findLowStockProducts()).thenReturn(lowStockProducts);

        List<Product> result = inventoryService.getLowStockProducts();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository, times(1)).findLowStockProducts();
    }

    @Test
    void getInventoryStats_ShouldReturnStatistics() {
        when(productRepository.countAll()).thenReturn(10L);
        when(productRepository.countActive()).thenReturn(8L);
        when(productRepository.findLowStockProducts()).thenReturn(List.of());
        when(productRepository.findOutOfStockProducts()).thenReturn(List.of());
        when(productRepository.findAll()).thenReturn(List.of(testProduct));
        when(productRepository.findAllCategories()).thenReturn(List.of("Medicine", "Vaccine"));

        Map<String, Object> stats = inventoryService.getInventoryStats();

        assertNotNull(stats);
        assertEquals(10L, stats.get("totalProducts"));
        assertEquals(8L, stats.get("activeProducts"));
        assertEquals(2L, stats.get("inactiveProducts"));
        assertEquals(0, stats.get("lowStockProducts"));
        assertEquals(0, stats.get("outOfStockProducts"));
        assertEquals(2, stats.get("categories"));
        assertNotNull(stats.get("timestamp"));
    }

    @Test
    void searchProducts_ShouldReturnSearchResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> expectedPage = new PageImpl<>(List.of(testProduct));
        
        when(productRepository.search("test", pageable)).thenReturn(expectedPage);

        Page<Product> result = inventoryService.searchProducts("test", null, null, null, null, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository, times(1)).search("test", pageable);
    }

    @Test
    void getCategories_ShouldReturnCategoriesWithCounts() {
        when(productRepository.findAllCategories()).thenReturn(Arrays.asList("Medicine", "Vaccine"));
        when(productRepository.findAll()).thenReturn(Arrays.asList(
                testProduct,
                Product.builder().category("Medicine").build(),
                Product.builder().category("Vaccine").build()
        ));

        List<Map<String, Object>> categories = inventoryService.getCategories();

        assertNotNull(categories);
        assertEquals(2, categories.size());
        verify(productRepository, times(1)).findAllCategories();
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getProductsByCategory_ShouldReturnProductsInCategory() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        Page<Product> expectedPage = new PageImpl<>(List.of(testProduct));
        
        when(productRepository.findByCategory("Medicine", pageable)).thenReturn(expectedPage);

        Page<Product> result = inventoryService.getProductsByCategory("Medicine", 1, 10, "name", "asc");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository, times(1)).findByCategory("Medicine", pageable);
    }

    @Test
    void getProductsByLocation_ShouldReturnProductsAtLocation() {
        when(productRepository.findByLocation("A1")).thenReturn(List.of(testProduct));

        List<Product> result = inventoryService.getProductsByLocation("A1");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository, times(1)).findByLocation("A1");
    }

    @Test
    void getProductValuation_ShouldReturnValuationData() {
        when(productRepository.findAll()).thenReturn(Arrays.asList(
                testProduct,
                Product.builder()
                        .stock(5)
                        .price(20.0)
                        .cost(10.0)
                        .build()
        ));

        Map<String, Object> valuation = inventoryService.getProductValuation();

        assertNotNull(valuation);
        assertNotNull(valuation.get("totalValue"));
        assertNotNull(valuation.get("stockValue"));
        assertNotNull(valuation.get("profitValue"));
        assertEquals(2, valuation.get("productCount"));
        assertNotNull(valuation.get("timestamp"));
    }

    @Test
    void updateProductThresholds_ShouldUpdateThresholds() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = inventoryService.updateProductThresholds(1L, 20, 50);

        assertNotNull(result);
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProductThresholds_ShouldThrowExceptionWhenProductNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> inventoryService.updateProductThresholds(999L, 10, 50));
        verify(productRepository, times(1)).findById(999L);
        verify(productRepository, never()).save(any(Product.class));
    }
}
