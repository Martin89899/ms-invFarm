package com.pawpet.msinvfarm.repository;

import com.pawpet.msinvfarm.model.StockHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockHistoryRepository extends JpaRepository<StockHistory, Long> {
    
    List<StockHistory> findByProductIdOrderByTimestampDesc(Long productId);
    
    Page<StockHistory> findByProductIdOrderByTimestampDesc(Long productId, Pageable pageable);
    
    @Query("SELECT sh FROM StockHistory sh WHERE sh.productId = :productId " +
           "AND (:dateFrom IS NULL OR sh.timestamp >= :dateFrom) " +
           "AND (:dateTo IS NULL OR sh.timestamp <= :dateTo) " +
           "ORDER BY sh.timestamp DESC")
    Page<StockHistory> findByProductIdWithDateRange(
            @Param("productId") Long productId,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable
    );
    
    @Query("SELECT COUNT(sh) FROM StockHistory sh WHERE sh.productId = :productId")
    Long countByProductId(@Param("productId") Long productId);
}
