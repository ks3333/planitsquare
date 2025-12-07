package com.planitsquare.holiday.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planitsquare.holiday.model.CountryInfoDto;
import com.planitsquare.holiday.model.HolidayCountryDto;
import com.planitsquare.holiday.model.HolidayInfoDto;
import com.planitsquare.holiday.model.HolidayTypeDto;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
public class HolidayInfoJdbcRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;


    public HolidayInfoJdbcRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    private static final String HOLIDAY_INFO_BULK_INSERT_SQL =
            "INSERT INTO holidayInfo (" +
                    "holidayYear, holidayDate, localName, name, countryCode, " +
                    "fixed, global, counties, launchYear, types" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String COUNTRY_INFO_BULK_INSERT_SQL =
            "INSERT INTO countryInfo (" +
                    "name, countryCode" +
                    ") VALUES (?, ?)";

    private static final String HOLIDAY_COUNTRY_BULK_INSERT_SQL =
            "INSERT INTO holidayCountry (" +
                    "holidayInfoSeq, country" +
                    ") VALUES (?, ?)";

    private static final String HOLIDAY_TYPE_BULK_INSERT_SQL =
            "INSERT INTO holidayType (" +
                    "holidayInfoSeq, type" +
                    ") VALUES (?, ?)";

    private static final String HOLIDAY_INFO_BULK_DELETE_SQL =
            "DELETE FROM holidayInfo " +
                    "WHERE holidayDay IN (:yearList)";

    private static final String HOLIDAY_COUNTRY_BULK_DELETE_SQL =
            "DELETE FROM holidayCountry " +
                    "WHERE holidayInfoSeq IN (:ids)";

    private static final String HOLIDAY_TYPE_BULK_DELETE_SQL =
            "DELETE FROM holidayType " +
                    "WHERE holidayInfoSeq IN (:ids)";

    private static final String COUNTRY_INFO_BULK_DELETE_SQL =
            "DELETE FROM countryInfo";
    public void holidayInfoBatchInsert(List<HolidayInfoDto> holidays) {

        jdbcTemplate.batchUpdate(HOLIDAY_INFO_BULK_INSERT_SQL, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                HolidayInfoDto dto = holidays.get(i);

                ps.setObject(1, dto.getHolidayYear());
                ps.setObject(2, dto.getDate());
                ps.setString(3, dto.getLocalName());
                ps.setString(4, dto.getName());
                ps.setString(5, dto.getCountryCode());
                ps.setBoolean(6, dto.isFixed());
                ps.setBoolean(7, dto.isGlobal());
                ps.setString(8, toListString(dto.getCounties()));
                ps.setObject(9, dto.getLaunchYear());
                ps.setString(10, toListString(dto.getTypes()));
            }

            @Override
            public int getBatchSize() {
                return holidays.size();
            }
        });
    }

    public void countryInfoBatchInsert(List<CountryInfoDto> holidays) {

        jdbcTemplate.batchUpdate(COUNTRY_INFO_BULK_INSERT_SQL, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                CountryInfoDto dto = holidays.get(i);

                ps.setString(1, dto.getName());
                ps.setString(2, dto.getCountryCode());
            }

            @Override
            public int getBatchSize() {
                return holidays.size();
            }
        });
    }

    public void holidayCountryBatchInsert(List<HolidayCountryDto> countryDtoList) {

        jdbcTemplate.batchUpdate(HOLIDAY_COUNTRY_BULK_INSERT_SQL, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                HolidayCountryDto dto = countryDtoList.get(i);

                ps.setLong(1, dto.getHolidayInfoSeq());
                ps.setString(2, dto.getCountry());
            }

            @Override
            public int getBatchSize() {
                return countryDtoList.size();
            }
        });
    }

    public void holidayTypeBatchInsert(List<HolidayTypeDto> holidayTypeDtoList) {

        jdbcTemplate.batchUpdate(HOLIDAY_TYPE_BULK_INSERT_SQL, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                HolidayTypeDto dto = holidayTypeDtoList.get(i);

                ps.setLong(1, dto.getHolidayInfoSeq());
                ps.setString(2, dto.getType());
            }

            @Override
            public int getBatchSize() {
                return holidayTypeDtoList.size();
            }
        });
    }

//    public void holidayInfoBatchDelete(List<Integer> yearList) {
//        jdbcTemplate.update(HOLIDAY_INFO_BULK_DELETE_SQL, new MapSqlParameterSource("yearList", yearList));
//    }
//
//    public void holidayCountryBatchDelete(List<Long> ids) {
//        jdbcTemplate.update(HOLIDAY_COUNTRY_BULK_DELETE_SQL, new MapSqlParameterSource("ids", ids));
//    }
//    public void holidayTypeBatchDelete(List<Long> ids) {
//        jdbcTemplate.update(HOLIDAY_TYPE_BULK_DELETE_SQL, new MapSqlParameterSource("ids", ids));
//    }

    public void countryInfoBatchDelete() {
        jdbcTemplate.update(COUNTRY_INFO_BULK_DELETE_SQL);
    }

    private String toJson(List<String> list) {
        try {
            return list == null ? null : objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }

    private String toListString(List<String> list) {
        if(list == null) {
            return null;
        }
        return String.join(",", list);

    }
}
