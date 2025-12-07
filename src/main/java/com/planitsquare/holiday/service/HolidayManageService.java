package com.planitsquare.holiday.service;

import com.planitsquare.holiday.Exception.DateNotExistException;
import com.planitsquare.holiday.Exception.RestClientCallException;
import com.planitsquare.holiday.constant.HolidaySortType;
import com.planitsquare.holiday.model.*;
import com.planitsquare.holiday.model.request.PageInfoRequest;
import com.planitsquare.holiday.store.ApiCallStore;
import com.planitsquare.holiday.store.HolidayManageStore;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

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

        List<CountryInfoDto> beforeCountryInfoList = holidayManageStore.getHCountryInfoAll();
        if(!ObjectUtils.isEmpty(beforeCountryInfoList)){
            holidayManageStore.countryInfoBatchDelete();
        }

        List<HolidayInfoDto> beforeHolidayInfoList = holidayManageStore.getHolidayInfoList(yearList.getLast(), yearList.getFirst());
        if(!ObjectUtils.isEmpty(beforeHolidayInfoList)){
            List<Long> deleteIds = beforeHolidayInfoList.stream().map(HolidayInfoDto::getHolidayInfoSeq).toList();
            holidayManageStore.holidayTypeBatchDelete(deleteIds);
            holidayManageStore.holidayCountryBatchDelete(deleteIds);
            holidayManageStore.holidayInfoBatchDelete(yearList);
        }

        holidayManageStore.countryInfoBatchInsert(countryInfoList);
        holidayManageStore.holidayInfoBatchInsert(holidayInfo);

        List<HolidayInfoDto> saveList = holidayManageStore.getHolidayInfoList(yearList.getLast(), yearList.getFirst());
        holidayManageStore.bulkInsertHolidayAdditionalInfos(saveList);

    }

    public Page<HolidayInfoDto> getHolidayInfoByCountry(String country, PageInfoRequest pageInfo, SearchFilterDto filter){
        if(!holidayManageStore.existsByCountryCode(country)){
            throw new DateNotExistException("Country code not exist: %s".formatted(country));
        }

        Sort sort = makeSortData(pageInfo);
        PageRequest pageable = PageRequest.of(pageInfo.getPage() -1, pageInfo.getSize(), sort);

        return holidayManageStore.getHolidayInfoByCountry(country, pageable, filter);
    }

    public Page<HolidayInfoDto> getHolidayInfoByYear(int year, PageInfoRequest pageInfo, SearchFilterDto filter){

        Sort sort = makeSortData(pageInfo);
        PageRequest pageable = PageRequest.of(pageInfo.getPage() -1, pageInfo.getSize(), sort);

        return holidayManageStore.getHolidayInfoByYear(year, pageable, filter);
    }

    private Sort makeSortData(PageInfoRequest pageInfo){
        if(pageInfo.getSortTarget() == null || HolidaySortType.valueOfString(pageInfo.getSortTarget()) == null){
            return Sort.by(ASC, HolidaySortType.DATE.name());
        }else {
            return Sort.by(pageInfo.getSort().equalsIgnoreCase(ASC.name()) ? ASC : DESC, pageInfo.getSortTarget());
        }
    }

    @Transactional(rollbackOn = Exception.class)
    public void refreshHolidayData(int year, String countryCode){
        List<HolidayInfoDto> holidayInfoList = apiStore.getHolidayList(year, countryCode);
        if(!holidayManageStore.existsHolidayInfoByYearAndCountryCode(year, countryCode)){
            holidayManageStore.holidayInfoBatchInsert(holidayInfoList);

            List<HolidayInfoDto> saveList = holidayManageStore.getHolidayInfoList(year, countryCode);
            holidayManageStore.bulkInsertHolidayAdditionalInfos(saveList);
        }else{
            holidayManageStore.refreshHolidayInfo(year, countryCode, holidayInfoList);
        }
    }

    @Transactional(rollbackOn = Exception.class)
    public void deleteHolidayInfo(int year, String countryCode){
        List<HolidayInfoDto> beforeHolidayInfoList = holidayManageStore.getHolidayInfoList(year, countryCode);
        if(!beforeHolidayInfoList.isEmpty()){
            List<Long> deleteIds = beforeHolidayInfoList.stream().map(HolidayInfoDto::getHolidayInfoSeq).toList();
            holidayManageStore.holidayTypeBatchDelete(deleteIds);
            holidayManageStore.holidayCountryBatchDelete(deleteIds);
            holidayManageStore.holidayInfoBatchDelete(year, countryCode);
        }
    }

    @Transactional(rollbackOn = Exception.class)
    public void scheduleSynchronizeHolidayData(int year){
        List<CountryInfoDto> countryInfoList = holidayManageStore.getHCountryInfoAll();
        List<String> countryList = countryInfoList.stream().map(CountryInfoDto::getCountryCode).toList();
        countryList.forEach(f -> refreshHolidayData(year, f));
    }

}
