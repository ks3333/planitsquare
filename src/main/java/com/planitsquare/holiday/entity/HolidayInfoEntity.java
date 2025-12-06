package com.planitsquare.holiday.entity;

import com.planitsquare.holiday.model.HolidayInfoDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name="holidayInfo")
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
    List<HolidayCountryEntity> countiesEntitysList;

    @OneToMany(mappedBy = "holidayInfo", fetch = FetchType.LAZY)
    List<HolidayTypeEntity> countiesTypeList;

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
}
