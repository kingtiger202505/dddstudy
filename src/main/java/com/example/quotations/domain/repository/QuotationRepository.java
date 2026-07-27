package com.example.quotations.domain.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.quotations.domain.model.Quotation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 报价单 Repository 接口
 */
@Mapper
public interface QuotationRepository extends BaseMapper<Quotation> {

    /**
     * 根据 ID 查询报价单
     */
    @Select("SELECT * FROM quotation WHERE id = #{id}")
    Quotation selectById(@Param("id") Long id);

    /**
     * 更新报价单 (带乐观锁)
     * @return 受影响的行数
     */
    @Update("UPDATE quotation SET " +
            "status = #{quotation.status}, " +
            "quoted_price = #{quotation.quotedPrice}, " +
            "expired_at = #{quotation.expiredAt}, " +
            "updated_at = #{quotation.updatedAt}, " +
            "version = version + 1 " +
            "WHERE id = #{quotation.id} AND version = #{version}")
    int updateWithOptimisticLock(@Param("quotation") Quotation quotation, 
                                  @Param("version") Integer version);

    /**
     * 查询所有报价单
     */
    @Select("SELECT * FROM quotation ORDER BY updated_at DESC")
    java.util.List<Quotation> findAll();
}
