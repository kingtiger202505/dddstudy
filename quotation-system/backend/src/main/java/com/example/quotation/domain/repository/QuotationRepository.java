package com.example.quotation.domain.repository;

import com.example.quotation.domain.model.Quotation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 报价单 Repository
 */
@Mapper
public interface QuotationRepository {

    /**
     * 根据ID查询
     */
    Quotation selectById(@Param("id") Long id);

    /**
     * 插入
     */
    int insert(Quotation quotation);

    /**
     * 带乐观锁的更新
     * SQL: UPDATE quotation SET ... WHERE id = #{id} AND version = #{version}
     */
    @Update("UPDATE quotation SET " +
            "product_id = #{quotation.productId}, " +
            "product_name = #{quotation.productName}, " +
            "original_price = #{quotation.originalPrice}, " +
            "current_price = #{quotation.currentPrice}, " +
            "min_acceptable_price = #{quotation.minAcceptablePrice}, " +
            "stock_quantity = #{quotation.stockQuantity}, " +
            "status = #{quotation.status}, " +
            "version = version + 1, " +
            "updated_at = NOW() " +
            "WHERE id = #{quotation.id} AND version = #{version}")
    int updateWithOptimisticLock(@Param("quotation") Quotation quotation, @Param("version") Integer version);

    /**
     * 直接根据ID和版本号更新库存（CAS方式）
     * 用于高并发场景下的库存扣减
     */
    @Update("UPDATE quotation SET " +
            "stock_quantity = stock_quantity - #{quantity}, " +
            "version = version + 1 " +
            "WHERE id = #{id} AND stock_quantity >= #{quantity} AND version = #{version}")
    int decreaseStockCas(@Param("id") Long id, @Param("quantity") Integer quantity, @Param("version") Integer version);
}
