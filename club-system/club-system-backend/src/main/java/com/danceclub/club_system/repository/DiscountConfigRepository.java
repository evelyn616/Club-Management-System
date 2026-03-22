package com.danceclub.club_system.repository;

import com.danceclub.club_system.model.DiscountConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountConfigRepository extends JpaRepository<DiscountConfig, Long> {
}
