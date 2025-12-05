package com.planitsquare.holiday.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name="holidayCountry")
public class HolidayCountryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long HolidayCountrySeq;

    @Column(insertable=false, updatable=false)
    Long holidayInfoSeq;

    @Column
    String Country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "holidayInfoSeq")
    HolidayInfoEntity holidayInfo;
}
