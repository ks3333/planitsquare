package com.planitsquare.holiday.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class HolidayCountryDto {

    Long HolidayCountrySeq;

    Long holidayInfoSeq;

    String Country;
}
