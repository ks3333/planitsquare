package com.planitsquare.holiday.repository;

import com.planitsquare.holiday.entity.CountryInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryInfoEntityRepository extends JpaRepository<CountryInfoEntity, Long> {
}