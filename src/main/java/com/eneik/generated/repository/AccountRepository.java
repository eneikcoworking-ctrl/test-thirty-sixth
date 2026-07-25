package com.eneik.generated.repository;

import com.eneik.generated.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByStatusAndIsAgedOrderByUsernameAsc(String status, boolean isAged);
    List<Account> findByIsAgedTrueOrderByUsernameAsc();
}
