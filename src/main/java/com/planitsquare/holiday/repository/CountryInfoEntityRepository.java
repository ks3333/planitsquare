package com.planitsquare.holiday.repository;

import com.planitsquare.holiday.entity.CountryInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CountryInfoEntityRepository extends JpaRepository<CountryInfoEntity, Long> {

    public Optional<CountryInfoEntity> findByCountryCode(String countryCode);
}