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


    /**
     * 공휴일 정보 일괄 삽입
     * @param holidays 공휴일 정보 리스트
     */
    public void holidayInfoBatchInsert(List<HolidayInfoDto> holidays) {
        Assert.notEmpty(holidays, "Holidays list must not be empty");
        holidayInfoJdbcRepository.holidayInfoBatchInsert(holidays);
    }

    /**
     * 국가코드 일괄 삽입
     * @param countries 국가코드 리스트
     */
    public void countryInfoBatchInsert(List<CountryInfoDto> countries) {
        Assert.notEmpty(countries, "Country list must not be empty");
        holidayInfoJdbcRepository.countryInfoBatchInsert(countries);
    }

    /**
     * 공휴일 지역 정보 일괄 삽입
     * @param countryDtoList 공휴일 지역 목록
     */
    public void holidayCountryBatchInsert(List<HolidayCountryDto> countryDtoList) {
        Assert.notEmpty(countryDtoList, "Holiday country list must not be empty");
        countryDtoList.stream().filter(f -> f.getCountry() == null || f.getHolidayInfoSeq() == null || f.getHolidayInfoSeq() == 0L).findAny().ifPresent(f -> {
            throw new IllegalArgumentException("HolidayCountryDto contains null values: %s".formatted(f.toString()));
        });
        holidayInfoJdbcRepository.holidayCountryBatchInsert(countryDtoList);
    }

    /**
     * 공휴일 유형 정보 일괄 삽입
     * @param holidayTypeDtoList 공휴일 유형 목록
     */
    public void holidayTypeBatchInsert(List<HolidayTypeDto> holidayTypeDtoList) {
        Assert.notEmpty(holidayTypeDtoList, "Holiday type list must not be empty");
        holidayTypeDtoList.stream().filter(f -> f.getType() == null || f.getHolidayInfoSeq() == null || f.getHolidayInfoSeq() == 0L).findAny().ifPresent(f -> {
            throw new IllegalArgumentException("holidayTypeDtoList contains null values: %s".formatted(f.toString()));
        });
        holidayInfoJdbcRepository.holidayTypeBatchInsert(holidayTypeDtoList);
    }

    /**
     * 공휴일 정보 일괄 삭제
     * @param yearList 삭제할 연도 리스트
     */
    public void holidayInfoBatchDelete(List<Integer> yearList) {
        Assert.notEmpty(yearList, "yearList list must not be empty");
        holidayInfoEntityRepository.holidayInfoBatchDelete(yearList);
    }

    /**
     * 공휴일 정보 일괄 삭제
     * @param year 삭제할 연도
     * @param countryCode 삭제할 국가코드
     */
    public void holidayInfoBatchDelete(int year, String countryCode) {
        Assert.notNull(year, "year must not be empty");
        Assert.notNull(countryCode, "countryCode must not be empty");
        holidayInfoEntityRepository.holidayInfoBatchDelete(year, countryCode);
    }

    /**
     * 공휴일 국가 정보 일괄 삭제
     * @param ids 삭제할 공휴일 정보 ID 리스트
     */
    public void holidayCountryBatchDelete(List<Long> ids) {
        Assert.notEmpty(ids, "Id list must not be empty");
        holidayCountryEntityRepository.holidayCountryBatchDelete(ids);
    }
    /**
     * 공휴일 유형 정보 일괄 삭제
     * @param ids 삭제할 공휴일 정보 ID 리스트
     */
    public void holidayTypeBatchDelete(List<Long> ids) {
        Assert.notEmpty(ids, "Id list must not be empty");
        holidayTypeEntityRepository.holidayTypeBatchDelete(ids);
    }

    /**
     * 국가 정보 일괄 삭제
     */
    public void countryInfoBatchDelete() {
        holidayInfoJdbcRepository.countryInfoBatchDelete();
    }

    /**
     * 전체 공휴일 정보 조회
     */
    public List<HolidayInfoDto> getHolidayInfoAll(){
        return holidayInfoEntityRepository.findAll().stream().map(HolidayInfoEntity::makeDto).toList();
    }

    /**
     * 전체 국가 정보 조회
     */
    public List<CountryInfoDto> getHCountryInfoAll(){
        return countryInfoEntityRepository.findAll().stream().map(CountryInfoEntity::makeDto).toList();
    }


    /**
     * 특정 연도 범위 내 공휴일 정보 조회
     * @param startYear 시작연도
     * @param endYear 종료연도
     */
    public List<HolidayInfoDto> getHolidayInfoList(Integer startYear, Integer endYear){
        Assert.notNull(startYear, "startYear must not be empty");
        Assert.notNull(endYear, "endYear must not be empty");
        return holidayInfoEntityRepository.findByHolidayYearBetween(startYear, endYear).stream().map(HolidayInfoEntity::makeDto).toList();
    }

    /**
     * 국가별 공휴일 정보 페이징 조회
     * @param country 국가 코드
     * @param pageable 페이지 요청 정보
     * @param filter 검색 필터
     */
    public Page<HolidayInfoDto> getHolidayInfoByCountry(String country, PageRequest pageable, SearchFilterDto filter){
        return holidayInfoQueryDslRepository.findHolidayInfoByCountry(country, pageable, filter);
    }

    /**
     * 연도별 공휴일 정보 페이징 조회
     * @param year 연도
     * @param pageable 페이지 요청 정보
     * @param filter 검색 필터
     */
    public Page<HolidayInfoDto> getHolidayInfoByYear(int year, PageRequest pageable, SearchFilterDto filter){
        return holidayInfoQueryDslRepository.findHolidayInfoByYear(year, pageable, filter);
    }

    /**
     * 국가 코드 존재 여부 확인
     * @param countryCode 국가 코드
     */
    public boolean existsByCountryCode(String countryCode) {
        return countryInfoEntityRepository.findByCountryCode(countryCode).isPresent();
    }

    /**
     * 특정 연도, 국가의 공휴일 리스트 조회
     * @param year 연도
     * @param countryCode 국가 코드
     */
    public List<HolidayInfoDto> getHolidayInfoList(int year, String countryCode){
        return getHolidayInfoEntityList(year, countryCode).stream().map(HolidayInfoEntity::makeDto).toList();
    }

    /**
     * 특정 연도, 국가의 공휴일 엔티티 리스트 조회
     * @param year 연도
     * @param countryCode 국가 코드
     */
    private List<HolidayInfoEntity> getHolidayInfoEntityList(int year, String countryCode) {
        return holidayInfoEntityRepository.findByHolidayYearAndCountryCode(year, countryCode);
    }

    /**
     * 특정 연도, 국가의 공휴일 정보 존재 여부 확인
     * @param year 연도
     * @param countryCode 국가 코드
     */
    public boolean existsHolidayInfoByYearAndCountryCode(int year, String countryCode) {
        return holidayInfoEntityRepository.countByHolidayYearAndCountryCode(year, countryCode).isPresent();
    }

    /**
     * 특정 연도, 국가의 공휴일 정보 갱신
     * @param year 연도
     * @param countryCode 국가 코드
     * @param newList 새로운 공휴일 정보 리스트
     */
    public void refreshHolidayInfo(int year, String countryCode, List<HolidayInfoDto> newList) {
        List<HolidayInfoEntity> oldList = getHolidayInfoEntityList(year, countryCode);
        Map<String, HolidayInfoEntity> oldMap = oldList.stream()
                .collect(Collectors.toMap(g -> g.getHolidayDate().toString() + g.getName(), Function.identity()));
        Map<String, HolidayInfoDto> newMap = newList.stream()
                .collect(Collectors.toMap(g -> g.getDate().toString() + g.getName(), Function.identity()));
        synchronizeHolidayData(oldMap, newMap);
    }

    /**
     * 공휴일 데이터 동기화
     * @param oldMap 기존 공휴일 데이터 맵
     * @param newMap 새로운 공휴일 데이터 맵
     */
    public void synchronizeHolidayData(Map<String, HolidayInfoEntity> oldMap,
                                       Map<String, HolidayInfoDto> newMap) {
        //기존 데이터와 비교해서 update/insert/delete를 각각 모아서 처리
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

    /**
     * 공휴일 정보 업데이트
     * @param entity 기존 공휴일 정보 엔티티
     * @param dto 새로운 공휴일 정보 DTO
     */
    private void updateHolidayInfo(HolidayInfoEntity entity, HolidayInfoDto dto) {
        //공휴일 유형 목록을 비교하여 변경된 항목이 있으면 업데이트 처리
        //sorted 후 비교
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

        //공휴일 지역 목록을 비교하여 변경된 항목이 있으면 업데이트 처리
        //sorted 후 비교
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

    /**
     * 공휴일 유형 데이터 삭제
     * @param entity 공휴일 유형 엔티티
     */
    private void deleteHolidayTypes(HolidayTypeEntity entity){
        entity.getHolidayInfo().getHolidayTypeEntityList().remove(entity);
        holidayTypeEntityRepository.delete(entity);
    }

    /**
     * 공휴일 국가 데이터 삭제
     * @param entity 공휴일 국가 엔티티
     */
    private void deleteHolidayCountry(HolidayCountryEntity entity){
        entity.getHolidayInfo().getHolidayCountryEntityList().remove(entity);
        holidayCountryEntityRepository.delete(entity);
    }

    /**
     * 공휴일 추가 정보 일괄 삽입
     * @param saveList 공휴일 정보 리스트
     */
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

    /**
     * 공휴일 정보 등록
     * @param dto 등록할 공휴일 정보 데이터
     */
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

    /**
     * 공휴일 정보 삭제
     * @param entity 삭제할 공휴일 정보 데이터
     */
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
