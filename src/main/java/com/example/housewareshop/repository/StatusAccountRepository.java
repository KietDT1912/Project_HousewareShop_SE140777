package com.example.housewareshop.repository;

import com.example.housewareshop.entity.StatusAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusAccountRepository extends JpaRepository<StatusAccount,Integer> {
}
