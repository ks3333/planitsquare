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
public class HolidayTypeDto {

    Long holidayTypesSeq;

    Long holidayInfoSeq;

    String type;
}
