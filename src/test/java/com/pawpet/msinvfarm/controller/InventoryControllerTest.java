package com.pawpet.msinvfarm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pawpet.msinvfarm.model.Product;
import com.pawpet.msinvfarm.model.StockHistory;
import com.pawpet.msinvfarm.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
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
    void getProducts_ShouldReturnPageOfProducts() throws Exception {
        Page<Product> page = new PageImpl<>(List.of(testProduct));
        when(inventoryService.getProducts(anyInt(), anyInt(), anyString(), anyString(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/inventory/products")
                        .param("page", "1")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Test Product"));
    }

    @Test
    void getProductById_ShouldReturnProductWhenExists() throws Exception {
        when(inventoryService.getProductById(1L)).thenReturn(Optional.of(testProduct));

        mockMvc.perform(get("/api/inventory/products/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void getProductById_ShouldReturn404WhenNotExists() throws Exception {
        when(inventoryService.getProductById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/inventory/products/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProductBySku_ShouldReturnProductWhenExists() throws Exception {
        when(inventoryService.getProductBySku("SKU001")).thenReturn(Optional.of(testProduct));

        mockMvc.perform(get("/api/inventory/products/sku/SKU001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("SKU001"))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void getProductBySku_ShouldReturn404WhenNotExists() throws Exception {
        when(inventoryService.getProductBySku("NONEXISTENT")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/inventory/products/sku/NONEXISTENT")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProduct_ShouldCreateAndReturnProduct() throws Exception {
        when(inventoryService.createProduct(any(Product.class))).thenReturn(testProduct);

        mockMvc.perform(post("/api/inventory/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void updateProduct_ShouldUpdateAndReturnProduct() throws Exception {
        when(inventoryService.updateProduct(anyLong(), any(Product.class))).thenReturn(testProduct);

        mockMvc.perform(put("/api/inventory/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void deleteProduct_ShouldDeleteProduct() throws Exception {
        mockMvc.perform(delete("/api/inventory/products/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateStock_ShouldUpdateStock() throws Exception {
        when(inventoryService.updateStock(anyLong(), anyInt(), anyString())).thenReturn(testProduct);

        mockMvc.perform(patch("/api/inventory/products/1/stock")
                        .param("quantity", "5")
                        .param("updateType", "SALE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void getLowStockProducts_ShouldReturnLowStockProducts() throws Exception {
        List<Product> lowStockProducts = List.of(testProduct);
        when(inventoryService.getLowStockProducts()).thenReturn(lowStockProducts);

        mockMvc.perform(get("/api/inventory/products/low-stock")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Product"));
    }

    @Test
    void getStockAlerts_ShouldReturnStockAlerts() throws Exception {
        List<Product> alertProducts = List.of(testProduct);
        when(inventoryService.getLowStockProducts()).thenReturn(alertProducts);

        mockMvc.perform(get("/api/inventory/alerts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Product"));
    }

    @Test
    void getStockHistory_ShouldReturnStockHistory() throws Exception {
        StockHistory history = StockHistory.builder()
                .id(1L)
                .productId(1L)
                .oldStock(10)
                .newStock(5)
                .change(-5)
                .updateType("SALE")
                .timestamp(LocalDateTime.now())
                .build();
        
        Page<StockHistory> page = new PageImpl<>(List.of(history));
        when(inventoryService.getStockHistory(anyLong(), anyInt(), anyInt(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/inventory/products/1/history")
                        .param("page", "1")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getInventoryStats_ShouldReturnStatistics() throws Exception {
        Map<String, Object> stats = Map.of(
                "totalProducts", 10L,
                "activeProducts", 8L,
                "inactiveProducts", 2L,
                "lowStockProducts", 2,
                "outOfStockProducts", 1,
                "totalValue", 1000.0,
                "categories", 3,
                "timestamp", LocalDateTime.now().toString()
        );
        
        when(inventoryService.getInventoryStats()).thenReturn(stats);

        mockMvc.perform(get("/api/inventory/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts").value(10L))
                .andExpect(jsonPath("$.activeProducts").value(8L));
    }

    @Test
    void searchProducts_ShouldReturnSearchResults() throws Exception {
        Page<Product> page = new PageImpl<>(List.of(testProduct));
        when(inventoryService.searchProducts(anyString(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/api/inventory/search")
                        .param("query", "test")
                        .param("page", "1")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Test Product"));
    }

    @Test
    void getCategories_ShouldReturnCategories() throws Exception {
        List<Map<String, Object>> categories = Arrays.asList(
                Map.of("name", "Medicine", "count", 5),
                Map.of("name", "Vaccine", "count", 3)
        );
        
        when(inventoryService.getCategories()).thenReturn(categories);

        mockMvc.perform(get("/api/inventory/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Medicine"));
    }

    @Test
    void getProductsByCategory_ShouldReturnProductsInCategory() throws Exception {
        Page<Product> page = new PageImpl<>(List.of(testProduct));
        when(inventoryService.getProductsByCategory(anyString(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page);

        mockMvc.perform(get("/api/inventory/category/Medicine")
                        .param("page", "1")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Test Product"));
    }

    @Test
    void getProductsByLocation_ShouldReturnProductsAtLocation() throws Exception {
        List<Product> products = List.of(testProduct);
        when(inventoryService.getProductsByLocation("A1")).thenReturn(products);

        mockMvc.perform(get("/api/inventory/location/A1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Product"));
    }

    @Test
    void getProductValuation_ShouldReturnValuation() throws Exception {
        Map<String, Object> valuation = Map.of(
                "totalValue", 1000.0,
                "stockValue", 500.0,
                "profitValue", 500.0,
                "productCount", 10,
                "timestamp", LocalDateTime.now().toString()
        );
        
        when(inventoryService.getProductValuation()).thenReturn(valuation);

        mockMvc.perform(get("/api/inventory/valuation")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalValue").value(1000.0))
                .andExpect(jsonPath("$.stockValue").value(500.0));
    }

    @Test
    void updateProductThresholds_ShouldUpdateThresholds() throws Exception {
        when(inventoryService.updateProductThresholds(anyLong(), anyInt(), anyInt())).thenReturn(testProduct);

        mockMvc.perform(patch("/api/inventory/products/1/thresholds")
                        .param("minStock", "10")
                        .param("maxStock", "50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }
}
