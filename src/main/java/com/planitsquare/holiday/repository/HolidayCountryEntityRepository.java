package com.planitsquare.holiday.repository;

import com.planitsquare.holiday.entity.HolidayCountryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HolidayCountryEntityRepository extends JpaRepository<HolidayCountryEntity, Long> {

    @Modifying
    @Query(value = "DELETE FROM holidayCountry" +
           " WHERE holidayInfoSeq IN :ids", nativeQuery = true)
    public void holidayCountryBatchDelete(List<Long> ids);

}
