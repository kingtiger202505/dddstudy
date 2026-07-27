package com.example.quotations.domain.repository;

import com.example.quotations.domain.model.ProductInventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 商品库存Repository接口
 */
@Mapper
public interface ProductInventoryRepository {

    /**
     * 根据ID查询库存
     */
    @Select("SELECT * FROM product_inventory WHERE id = #{id}")
    ProductInventory selectById(@Param("id") Long id);

    /**
     * 根据商品ID查询库存
     */
    @Select("SELECT * FROM product_inventory WHERE product_id = #{productId}")
    ProductInventory selectByProductId(@Param("productId") Long productId);

    /**
     * 插入库存记录
     */
    int insert(ProductInventory inventory);

    /**
     * 扣减库存 (CAS操作，带乐观锁)
     * SQL中使用 stock_quantity >= #{quantity} 确保不会扣减为负
     * @return 受影响的行数 (1表示成功，0表示失败)
     */
    @Update("UPDATE product_inventory SET " +
            "stock_quantity = stock_quantity - #{quantity}, " +
            "version = version + 1 " +
            "WHERE product_id = #{productId} " +
            "AND stock_quantity >= #{quantity} " +
            "AND version = #{version}")
    int deductStock(@Param("productId") Long productId, 
                    @Param("quantity") Integer quantity,
                    @Param("version") Integer version);

    /**
     * 恢复库存 (带乐观锁)
     */
    @Update("UPDATE product_inventory SET " +
            "stock_quantity = stock_quantity + #{quantity}, " +
            "version = version + 1 " +
            "WHERE product_id = #{productId} AND version = #{version}")
    int restoreStock(@Param("productId") Long productId,
                     @Param("quantity") Integer quantity,
                     @Param("version") Integer version);
}
