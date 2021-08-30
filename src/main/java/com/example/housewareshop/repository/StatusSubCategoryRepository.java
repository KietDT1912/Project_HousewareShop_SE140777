package com.example.housewareshop.repository;

import com.example.housewareshop.entity.StatusSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusSubCategoryRepository extends JpaRepository<StatusSubCategory,Integer> {
}
