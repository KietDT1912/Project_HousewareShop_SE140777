package com.example.housewareshop.repository;

import com.example.housewareshop.entity.StatusCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusCategoryRepository extends JpaRepository<StatusCategory,Integer> {
}
