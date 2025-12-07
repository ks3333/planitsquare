package com.planitsquare.holiday.repository;

import com.planitsquare.holiday.entity.HolidayInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface HolidayInfoEntityRepository extends JpaRepository<HolidayInfoEntity, Long> {

    public List<HolidayInfoEntity> findByHolidayYearBetween(Integer startYear, Integer endYear);

    @Modifying
    @Query(value = "DELETE FROM holidayInfo" +
            " WHERE holidayYear IN :yearList", nativeQuery = true)
    public void holidayInfoBatchDelete(List<Integer> yearList);

    @Modifying
    @Query(value = "DELETE FROM holidayInfo" +
            " WHERE holidayYear = :year AND countryCode = :countryCode", nativeQuery = true)
    public void holidayInfoBatchDelete(int year,String countryCode);

    public List<HolidayInfoEntity> findByHolidayYearAndCountryCode(int year, String countryCode);

    public Optional<HolidayInfoEntity> findFirstByHolidayYearAndCountryCode(int year, String countryCode);
}
