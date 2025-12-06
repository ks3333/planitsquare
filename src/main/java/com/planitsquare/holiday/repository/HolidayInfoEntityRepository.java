package com.planitsquare.holiday.repository;

import com.planitsquare.holiday.entity.HolidayInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HolidayInfoEntityRepository extends JpaRepository<HolidayInfoEntity, Long> {

    public List<HolidayInfoEntity> findByHolidayYearBetween(Integer startYear, Integer endYear);

    @Modifying
    @Query(value = "DELETE FROM holidayInfo" +
            " WHERE holidayYear IN :yearList", nativeQuery = true)
    public void holidayInfoBatchDelete(List<Integer> yearList);
}
