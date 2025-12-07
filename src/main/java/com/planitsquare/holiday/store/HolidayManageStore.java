package com.planitsquare.holiday.store;

import com.mysema.commons.lang.Assert;
import com.planitsquare.holiday.entity.CountryInfoEntity;
import com.planitsquare.holiday.entity.HolidayCountryEntity;
import com.planitsquare.holiday.entity.HolidayInfoEntity;
import com.planitsquare.holiday.entity.HolidayTypeEntity;
import com.planitsquare.holiday.model.*;
import com.planitsquare.holiday.repository.HolidayInfoJdbcRepository;
import com.planitsquare.holiday.repository.CountryInfoEntityRepository;
import com.planitsquare.holiday.repository.HolidayCountryEntityRepository;
import com.planitsquare.holiday.repository.HolidayInfoEntityRepository;
import com.planitsquare.holiday.repository.HolidayTypeEntityRepository;
import com.planitsquare.holiday.repository.HolidayInfoQueryDslRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public List<HolidayInfoDto> getHolidayInfoList(int year, String countryCode){
        return getHolidayInfoEntityList(year, countryCode).stream().map(HolidayInfoEntity::makeDto).toList();
    }
    private List<HolidayInfoEntity> getHolidayInfoEntityList(int year, String countryCode) {
        return holidayInfoEntityRepository.findByHolidayYearAndCountryCode(year, countryCode);
    }

    public boolean existsHolidayInfoByYearAndCountryCode(int year, String countryCode) {
        return holidayInfoEntityRepository.findFirstByHolidayYearAndCountryCode(year, countryCode).isPresent();
    }
    public void refreshHolidayInfo(int year, String countryCode, List<HolidayInfoDto> newList) {
        List<HolidayInfoEntity> oldList = getHolidayInfoEntityList(year, countryCode);
        Map<String, HolidayInfoEntity> oldMap = oldList.stream()
                .collect(Collectors.toMap(g -> g.getHolidayDate().toString() + g.getName(), Function.identity()));
        Map<String, HolidayInfoDto> newMap = newList.stream()
                .collect(Collectors.toMap(g -> g.getDate().toString() + g.getName(), Function.identity()));
        synchronizeHolidayData(oldMap, newMap);
    }

    public void synchronizeHolidayData(Map<String, HolidayInfoEntity> oldMap,
                                       Map<String, HolidayInfoDto> newMap) {
        Set<String> newDateSet = newMap.keySet();
        Set<String> oldDateSet = oldMap.keySet().stream().map(String::new).collect(Collectors.toSet());
        Set<String> updateDateSet = new HashSet<>();
        List<HolidayInfoEntity> deleteList = new ArrayList<>();
        List<HolidayInfoDto> insertList = new ArrayList<>();

        for (String k : newDateSet) {

            if(oldDateSet.contains(k)){
                updateDateSet.add(k);
                oldDateSet.remove(k);
            }else{
                insertList.add(newMap.get(k));
            }

        }

        for (String date : oldDateSet) {
            deleteList.add(oldMap.get(date));
        }

        updateDateSet.forEach(date -> {
            HolidayInfoEntity entity = oldMap.get(date);
            HolidayInfoDto dto = newMap.get(date);
            updateHolidayInfo(entity, dto);
        });

        if(!insertList.isEmpty()){
            insertList.forEach(this::insertHolidayInfo);
        }

        if(!deleteList.isEmpty()){
            deleteList.forEach(this::deleteHolidayInfo);
        }

    }

    private void updateHolidayInfo(HolidayInfoEntity entity, HolidayInfoDto dto) {
        if(entity.getTypes() != null){
            List<String> oriTypesStr = Stream.of(entity.getTypes().split(",")).sorted().toList();
            List<String> newTypesList = dto.getTypes().stream().sorted().toList();

            if(oriTypesStr.size() == newTypesList.size()){
                if(!String.join(",", oriTypesStr).equals(String.join(",", newTypesList))){
                    List<HolidayTypeEntity> types = entity.getHolidayTypeEntityList();
                    for(int i=0; i<types.size(); i++){
                        HolidayTypeEntity typeEntity = types.get(i);
                        typeEntity.updateType(newTypesList.get(i));
                        holidayTypeEntityRepository.save(typeEntity);
                    }
                }
            }else{
                if(oriTypesStr.size() > newTypesList.size()) {
                    //삭제
                    List<HolidayTypeEntity> types = entity.getHolidayTypeEntityList();
                    for (int i = newTypesList.size(); i < types.size(); i++) {
                        HolidayTypeEntity typeEntity = types.get(i);
                        deleteHolidayTypes(typeEntity);
                    }
                    for (int i = 0; i < newTypesList.size(); i++) {
                        HolidayTypeEntity typeEntity = types.get(i);
                        typeEntity.updateType(newTypesList.get(i));
                        holidayTypeEntityRepository.save(typeEntity);
                    }
                }else {
                    //추가
                    List<HolidayTypeEntity> types = entity.getHolidayTypeEntityList();
                    for (int i = 0; i < types.size(); i++) {
                        HolidayTypeEntity typeEntity = types.get(i);
                        typeEntity.updateType(newTypesList.get(i));
                        holidayTypeEntityRepository.save(typeEntity);
                    }
                    for (int i = types.size(); i < newTypesList.size(); i++) {
                        HolidayTypeEntity newTypeEntity = new HolidayTypeEntity();
                        holidayTypeEntityRepository.save(new HolidayTypeEntity(newTypesList.get(i), entity));
                    }
                }
            }
        }

        if(entity.getCounties() != null){
            List<String> oriCountiesStr = List.of(entity.getCounties().split(","));
            List<String> newCountiesStr = List.of(entity.getCounties().split(","));
            if(oriCountiesStr.size() == newCountiesStr.size()){
                if(!String.join(",", oriCountiesStr).equals(String.join(",", newCountiesStr))){
                    List<HolidayCountryEntity> types = entity.getHolidayCountryEntityList();
                    for(int i=0; i<types.size(); i++){
                        HolidayCountryEntity countryEntity = types.get(i);
                        countryEntity.updateCountry(newCountiesStr.get(i));
                        holidayCountryEntityRepository.save(countryEntity);
                    }
                }
            }else{
                if(oriCountiesStr.size() > newCountiesStr.size()) {
                    //삭제
                    List<HolidayCountryEntity> counties = entity.getHolidayCountryEntityList();
                    for (int i = newCountiesStr.size(); i < counties.size(); i++) {
                        HolidayCountryEntity countryEntity = counties.get(i);
                        deleteHolidayCountry(countryEntity);
                    }
                    for (int i = 0; i < newCountiesStr.size(); i++) {
                        HolidayCountryEntity countryEntity = counties.get(i);
                        countryEntity.updateCountry(newCountiesStr.get(i));
                        holidayCountryEntityRepository.save(countryEntity);
                    }
                }else {
                    //추가
                    List<HolidayCountryEntity> counties = entity.getHolidayCountryEntityList();
                    for (int i = 0; i < counties.size(); i++) {
                        HolidayCountryEntity countryEntity = counties.get(i);
                        countryEntity.updateCountry(newCountiesStr.get(i));
                        holidayCountryEntityRepository.save(countryEntity);
                    }
                    for (int i = counties.size(); i < newCountiesStr.size(); i++) {
                        holidayCountryEntityRepository.save(new HolidayCountryEntity(newCountiesStr.get(i), entity));
                    }
                }
            }
        }

        entity.updateEntity(dto);
        holidayInfoEntityRepository.save(entity);
    }

    private void deleteHolidayTypes(HolidayTypeEntity entity){
        entity.getHolidayInfo().getHolidayTypeEntityList().remove(entity);
        holidayTypeEntityRepository.delete(entity);
    }

    private void deleteHolidayCountry(HolidayCountryEntity entity){
        entity.getHolidayInfo().getHolidayCountryEntityList().remove(entity);
        holidayCountryEntityRepository.delete(entity);
    }

    public void bulkInsertHolidayAdditionalInfos(List<HolidayInfoDto> saveList){
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
            holidayCountryBatchInsert(holidayCountryList);
        }

        if(!ObjectUtils.isEmpty(holidayTypeList)){
            holidayTypeBatchInsert(holidayTypeList);
        }
    }

    private void insertHolidayInfo(HolidayInfoDto dto){
        HolidayInfoEntity entity = new HolidayInfoEntity(dto);
        holidayInfoEntityRepository.save(entity);
        if(entity.getCounties() != null){
            List<String> newCountiesStr = List.of(entity.getCounties().split(","));
            newCountiesStr.forEach(f -> {
                holidayCountryEntityRepository.save(new HolidayCountryEntity(f, entity));
            });
        }

        if(entity.getTypes() != null){
            List<String> newTypesStr = List.of(entity.getTypes().split(","));
            newTypesStr.forEach(f -> {
                holidayTypeEntityRepository.save(new HolidayTypeEntity(f, entity));
            });
        }
    }

    private void deleteHolidayInfo(HolidayInfoEntity entity){

        if(!entity.getHolidayCountryEntityList().isEmpty()){
            entity.getHolidayCountryEntityList().forEach(this::deleteHolidayCountry);
        }

        if(!entity.getHolidayTypeEntityList().isEmpty()){
            entity.getHolidayTypeEntityList().forEach(this::deleteHolidayTypes);
        }

        holidayInfoEntityRepository.delete(entity);

    }
}
