package com.planitsquare.holiday.entity;

import com.planitsquare.holiday.model.HolidayInfoDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name="holidayInfo", uniqueConstraints = {
        @UniqueConstraint(
                name="holidayInfo_holidayDate_name_countryCode",
                columnNames={"holidayDate", "name", "countryCode"}
        )})
public class HolidayInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long holidayInfoSeq;

    @NotNull
    @Column
    Integer holidayYear;

    @NotNull
    @Column
    LocalDate holidayDate;

    @Column
    String localName;

    @NotNull
    @Column
    String name;

    @NotNull
    @Column
    String countryCode;

    @Column
    boolean fixed;

    @Column
    boolean global;

    @Column(length = 4000)
    String counties;

    @Column
    Integer launchYear;

    @Column
    String types;

    @OneToMany(mappedBy = "holidayInfo", fetch = FetchType.LAZY)
    List<HolidayCountryEntity> holidayCountryEntityList;

    @OneToMany(mappedBy = "holidayInfo", fetch = FetchType.LAZY)
    List<HolidayTypeEntity> holidaytypeList;

    public HolidayInfoEntity (HolidayInfoDto dto){
        this.holidayYear = dto.getHolidayYear();
        this.holidayDate = dto.getDate();
        this.localName = dto.getLocalName();
        this.name = dto.getName();
        this.countryCode = dto.getCountryCode();
        this.fixed = dto.isFixed();
        this.global = dto.isGlobal();
        this.counties = dto.getCounties() != null ? String.join(",", dto.getCounties()) : null;
        this.launchYear = dto.getLaunchYear();
        this.types = dto.getTypes() != null ? String.join(",", dto.getTypes()) : null;
    }

    public List<HolidayCountryEntity> getHolidayCountryEntityList() {
        if(this.holidayCountryEntityList == null) {
            holidayCountryEntityList = new ArrayList<HolidayCountryEntity>();
        }

        return this.holidayCountryEntityList;
    }

    public List<HolidayTypeEntity> getHolidayTypeEntityList() {
        if(this.holidaytypeList == null) {
            holidaytypeList = new ArrayList<HolidayTypeEntity>();
        }

        return this.holidaytypeList;
    }

    public HolidayInfoDto makeDto() {
        return HolidayInfoDto.builder()
                .holidayInfoSeq(this.holidayInfoSeq)
                .holidayYear(this.holidayYear)
                .date(this.holidayDate)
                .localName(this.localName)
                .name(this.name)
                .countryCode(this.countryCode)
                .fixed(this.fixed)
                .global(this.global)
                .counties(this.counties != null ? List.of(this.counties.split(",")) : null)
                .launchYear(this.launchYear)
                .types(this.types != null ? List.of(this.types.split(",")) : null)
                .build();
    }

    public HolidayInfoEntity updateEntity(HolidayInfoDto dto){
        this.holidayYear = dto.getHolidayYear();
        this.holidayDate = dto.getDate();
        this.localName = dto.getLocalName();
        this.name = dto.getName();
        this.countryCode = dto.getCountryCode();
        this.fixed = dto.isFixed();
        this.global = dto.isGlobal();
        this.counties = dto.getCounties() != null ? String.join(",", dto.getCounties()) : null;
        this.launchYear = dto.getLaunchYear();
        this.types = dto.getTypes() != null ? String.join(",", dto.getTypes()) : null;
        return this;
    }
}
