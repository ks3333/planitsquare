package com.planitsquare.holiday.store;

import com.mysema.commons.lang.Assert;
import com.planitsquare.holiday.entity.CountryInfoEntity;
import com.planitsquare.holiday.entity.HolidayInfoEntity;
import com.planitsquare.holiday.model.*;
import com.planitsquare.holiday.repository.HolidayInfoJdbcRepository;
import com.planitsquare.holiday.repository.CountryInfoEntityRepository;
import com.planitsquare.holiday.repository.HolidayCountryEntityRepository;
import com.planitsquare.holiday.repository.HolidayInfoEntityRepository;
import com.planitsquare.holiday.repository.HolidayTypeEntityRepository;
import com.planitsquare.holiday.repository.HolidayInfoQueryDslRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class HolidayManageStore {


    private final HolidayInfoJdbcRepository holidayInfoJdbcRepository;

    private final HolidayCountryEntityRepository holidayCountryEntityRepository;

    private final HolidayInfoEntityRepository holidayInfoEntityRepository;

    private final HolidayTypeEntityRepository holidayTypeEntityRepository;

    private final CountryInfoEntityRepository countryInfoEntityRepository;

    private final HolidayInfoQueryDslRepository holidayInfoQueryDslRepository;

    public HolidayManageStore(HolidayInfoJdbcRepository holidayInfoJdbcRepository, HolidayCountryEntityRepository holidayCountryEntityRepository, HolidayInfoEntityRepository holidayInfoEntityRepository, HolidayTypeEntityRepository holidayTypeEntityRepository, CountryInfoEntityRepository countryInfoEntityRepository, HolidayInfoQueryDslRepository holidayInfoQueryDslRepository) {
        this.holidayInfoJdbcRepository = holidayInfoJdbcRepository;
        this.holidayCountryEntityRepository = holidayCountryEntityRepository;
        this.holidayInfoEntityRepository = holidayInfoEntityRepository;
        this.holidayTypeEntityRepository = holidayTypeEntityRepository;
        this.countryInfoEntityRepository = countryInfoEntityRepository;
        this.holidayInfoQueryDslRepository = holidayInfoQueryDslRepository;
    }


    public void holidayInfoBatchInsert(List<HolidayInfoDto> holidays) {
        Assert.notEmpty(holidays, "Holidays list must not be empty");
        holidayInfoJdbcRepository.holidayInfoBatchInsert(holidays);
    }

    public void countryInfoBatchInsert(List<CountryInfoDto> countries) {
        Assert.notEmpty(countries, "Country list must not be empty");
        holidayInfoJdbcRepository.countryInfoBatchInsert(countries);
    }

    public void holidayCountryBatchInsert(List<HolidayCountryDto> countryDtoList) {
        Assert.notEmpty(countryDtoList, "Holiday country list must not be empty");
        countryDtoList.stream().filter(f -> f.getCountry() == null || f.getHolidayInfoSeq() == null || f.getHolidayInfoSeq() == 0L).findAny().ifPresent(f -> {
            throw new IllegalArgumentException("HolidayCountryDto contains null values: %s".formatted(f.toString()));
        });
        holidayInfoJdbcRepository.holidayCountryBatchInsert(countryDtoList);
    }

    public void holidayTypeBatchInsert(List<HolidayTypeDto> holidayTypeDtoList) {
        Assert.notEmpty(holidayTypeDtoList, "Holiday type list must not be empty");
        holidayTypeDtoList.stream().filter(f -> f.getType() == null || f.getHolidayInfoSeq() == null || f.getHolidayInfoSeq() == 0L).findAny().ifPresent(f -> {
            throw new IllegalArgumentException("holidayTypeDtoList contains null values: %s".formatted(f.toString()));
        });
        holidayInfoJdbcRepository.holidayTypeBatchInsert(holidayTypeDtoList);
    }

    public void holidayInfoBatchDelete(List<Integer> yearList) {
        Assert.notEmpty(yearList, "yearList list must not be empty");
        holidayInfoEntityRepository.holidayInfoBatchDelete(yearList);
    }

    public void holidayCountryBatchDelete(List<Long> ids) {
        Assert.notEmpty(ids, "Id list must not be empty");
        holidayCountryEntityRepository.holidayCountryBatchDelete(ids);
    }
    public void holidayTypeBatchDelete(List<Long> ids) {
        Assert.notEmpty(ids, "Id list must not be empty");
        holidayTypeEntityRepository.holidayTypeBatchDelete(ids);
    }

    public void countryInfoBatchDelete() {
        holidayInfoJdbcRepository.countryInfoBatchDelete();
    }

    public List<HolidayInfoDto> getHolidayInfoAll(){
        return holidayInfoEntityRepository.findAll().stream().map(HolidayInfoEntity::makeDto).toList();
    }

    public List<CountryInfoDto> getHCountryInfoAll(){
        return countryInfoEntityRepository.findAll().stream().map(CountryInfoEntity::makeDto).toList();
    }

    public List<HolidayInfoDto> getHolidayInfoList(Integer startYear, Integer endYear){
        Assert.notNull(startYear, "startYear must not be empty");
        Assert.notNull(endYear, "endYear must not be empty");
        return holidayInfoEntityRepository.findByHolidayYearBetween(startYear, endYear).stream().map(HolidayInfoEntity::makeDto).toList();
    }

    public Page<HolidayInfoDto> getHolidayInfoByCountry(String country, PageRequest pageable, SearchFilterDto filter){
        return holidayInfoQueryDslRepository.findHolidayInfoByCountry(country, pageable, filter);
    }

    public Page<HolidayInfoDto> getHolidayInfoByYear(int year, PageRequest pageable, SearchFilterDto filter){
        return holidayInfoQueryDslRepository.findHolidayInfoByYear(year, pageable, filter);
    }

    public boolean existsByCountryCode(String countryCode) {
        return countryInfoEntityRepository.findByCountryCode(countryCode).isPresent();
    }
//    public List<HolidayInfoDto> getHolidayInfoByYear(int year, int page, int size) {
//        return holidayInfoQueryDslRepository.findHolidayInfoByYear(year, PageRequest.of(page, size));
//    }
}
