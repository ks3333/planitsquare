package com.planitsquare.holiday.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class HolidayInfoDto {

    Long holidayInfoSeq;

    Integer holidayYear;

    LocalDate date;

    String localName;

    String name;

    String countryCode;

    boolean fixed;

    boolean global;

    List<String> counties;

    Integer launchYear;

    List<String> types;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HolidayInfoDto that = (HolidayInfoDto) o;
        return Objects.equals(date, that.date) && Objects.equals(name, that.name) && Objects.equals(countryCode, that.countryCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, name, countryCode);
    }
}
