package com.example.nguyenthanhdat.repository;

import com.example.nguyenthanhdat.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryId(Long categoryId);

    // Sản phẩm khuyến mãi lên đầu, sau đó là không khuyến mãi
    @Query("SELECT p FROM Product p ORDER BY p.promoted DESC, p.id DESC")
    List<Product> findAllOrderByPromotedDesc();

    // Chỉ sản phẩm đang khuyến mãi
    List<Product> findByPromotedTrue();

    // Sản phẩm theo danh mục, khuyến mãi lên đầu
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId ORDER BY p.promoted DESC, p.id DESC")
    List<Product> findByCategoryIdOrderByPromotedDesc(Long categoryId);
}
