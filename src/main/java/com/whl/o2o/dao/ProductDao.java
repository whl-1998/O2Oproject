package com.whl.o2o.dao;

import com.whl.o2o.entity.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductDao {

    /**
     * 分页查询product
     * @param productCondition
     * @param rowIndex 第几行开始取值
     * @param pageSize 返回条数
     * @return
     */
    List<Product> queryProductList(@Param("productCondition") Product productCondition, @Param("rowIndex") int rowIndex, @Param("pageSize") int pageSize);

    /**
     * 满足productCondition条件的商品总数
     * @param productCondition
     * @return
     */
    int queryProductCount(@Param("productCondition") Product productCondition);

    /**
     * 添加商品
     * @param product
     * @return
     */
    int insertProduct(Product product);

    /**
     * 更新商品
     * @param product
     * @return
     */
    int updateProduct(Product product);


    /**
     * 通过id检索指定Product
     * @param productId
     * @return
     */
    Product queryProductById(Long productId);

    /**
     * 删除商品类别前,将商品表的商品类别id置为空
     * @param productCategoryId
     * @return
     */
    int updateProductCategoryToNull(long productCategoryId);
}
