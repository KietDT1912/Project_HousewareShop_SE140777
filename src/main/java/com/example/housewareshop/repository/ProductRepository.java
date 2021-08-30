package com.example.housewareshop.repository;

import com.example.housewareshop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query(value = "select p from Product p inner join p.subCategory as S where S.category.id = ?1",    // Nếu chỉ trả về 1 list thì câu query bày là đủ
            countQuery = "select count(p) from Product p inner join p.subCategory as S where S.category.id = ?1")
    // Nếu trả về kiểu page thì phải thêm câu query đếm

    public Page<Product> findByCategoryId(int categoryId, Pageable pageable);

    public Page<Product> findBySubCategoryId(int subCategoryId, Pageable pageable);

    @Query(value = "SELECT p from Product p where p.name like ?1 or p.description like ?1",
            countQuery = "SELECT count(p) from Product p where p.name like ?1 or p.description like ?1")
    public Page<Product> search(String keyword, Pageable pageable);
}
