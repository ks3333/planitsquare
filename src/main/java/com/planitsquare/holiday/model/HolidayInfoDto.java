package com.planitsquare.holiday.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

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
}
