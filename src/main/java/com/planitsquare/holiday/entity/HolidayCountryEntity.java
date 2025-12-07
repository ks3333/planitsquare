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
    String country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "holidayInfoSeq")
    HolidayInfoEntity holidayInfo;

    public HolidayCountryEntity(String country, HolidayInfoEntity holidayInfo) {
        this.country = country;
        this.seHolidayInfo(holidayInfo);
    }
    public void seHolidayInfo(HolidayInfoEntity holidayInfo) {

        if(this.holidayInfo != null) {
            this.holidayInfo.getHolidayTypeEntityList().remove(this);
        }

        this.holidayInfo = holidayInfo;
        holidayInfo.getHolidayCountryEntityList().add(this);

    }

    public void updateCountry(String country) {
        this.country = country;
    }
}
