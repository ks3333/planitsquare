package com.planitsquare.holiday.repository;

import com.planitsquare.holiday.entity.HolidayTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface HolidayTypeEntityRepository extends JpaRepository<HolidayTypeEntity, Long> {


    @Modifying
    @Query(value = "DELETE FROM HolidayType" +
            " WHERE holidayInfoSeq IN :ids", nativeQuery = true)
    public void holidayTypeBatchDelete(java.util.List<Long> ids);
}
