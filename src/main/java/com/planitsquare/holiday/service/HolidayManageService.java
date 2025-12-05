package com.planitsquare.holiday.service;

import com.planitsquare.holiday.Exception.RestClientCallException;
import com.planitsquare.holiday.model.CountryInfoDto;
import com.planitsquare.holiday.model.HolidayCountryDto;
import com.planitsquare.holiday.model.HolidayInfoDto;
import com.planitsquare.holiday.model.HolidayTypeDto;
import com.planitsquare.holiday.store.ApiCallStore;
import com.planitsquare.holiday.store.HolidayManageStore;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service
@Slf4j
public class HolidayManageService {

    private final ApiCallStore apiStore;

    private final HolidayManageStore holidayManageStore;


    public HolidayManageService(ApiCallStore apiStore, HolidayManageStore holidayManageStore) {
        this.apiStore = apiStore;
        this.holidayManageStore = holidayManageStore;
    }

    @Transactional(rollbackOn = Exception.class)
    public void initHolidayData(){
        List<CountryInfoDto> countryInfoList = apiStore.getCountryCodeList();
        List<String> countryList = countryInfoList.stream().map(CountryInfoDto::getCountryCode).toList();

        LocalDate now = LocalDate.now();

        List<Integer> yearList = IntStream.rangeClosed(0, 5)
                .mapToObj(now::minusYears)
                .map(LocalDate::getYear)
                .toList();
        List<HolidayInfoDto> holidayInfo = null;
        try {
            holidayInfo = apiStore.getHolidayInfoList(yearList, countryList);
        }catch (RestClientCallException re){
            log.error("RestClientCallException during API calls for holiday info", re);
            log.error("ERROR DETAIL : {}", re.toString());
            throw re;
        } catch (Exception e){
            log.error("Error during API calls for holiday info", e);
            throw e;
        }

        holidayManageStore.countryInfoBatchInsert(countryInfoList);
        holidayManageStore.holidayInfoBatchInsert(holidayInfo);

        List<HolidayInfoDto> saveList = holidayManageStore.getHolidayInfoAll();
        List<HolidayCountryDto> holidayCountryList = new ArrayList<>();
        List<HolidayTypeDto> holidayTypeList = new ArrayList<>();
        for(HolidayInfoDto info : saveList){
            Long holidayInfoSeq = info.getHolidayInfoSeq();
            List<String> countryCode = info.getCounties();
            if(!ObjectUtils.isEmpty(countryCode)){
                countryCode.forEach(country -> {
                    holidayCountryList.add(HolidayCountryDto.builder().holidayInfoSeq(holidayInfoSeq).Country(country).build());
                });
            }

            List<String> types = info.getTypes();
            if(!ObjectUtils.isEmpty(types)){
                types.forEach(type -> {
                    holidayTypeList.add(HolidayTypeDto.builder().holidayInfoSeq(holidayInfoSeq).type(type).build());
                });
            }


        }
        if(!ObjectUtils.isEmpty(holidayCountryList)){
            holidayManageStore.holidayCountryBatchInsert(holidayCountryList);
        }

        if(!ObjectUtils.isEmpty(holidayTypeList)){
            holidayManageStore.holidayTypeBatchInsert(holidayTypeList);
        }

    }

}
