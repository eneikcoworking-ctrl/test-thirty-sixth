package com.eneik.generated.repository;

import com.eneik.generated.model.SpintaxConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpintaxConfigRepository extends JpaRepository<SpintaxConfig, Long> {
    Optional<SpintaxConfig> findByCampaignId(Long campaignId);
}
