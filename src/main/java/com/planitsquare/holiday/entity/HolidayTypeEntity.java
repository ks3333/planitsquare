package com.planitsquare.holiday.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name="holidayType")
public class HolidayTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long holidayTypeSeq;

    @Column(insertable=false, updatable=false)
    Long holidayInfoSeq;

    @NotNull
    @Column
    String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "holidayInfoSeq")
    HolidayInfoEntity holidayInfo;

    public HolidayTypeEntity(String type, HolidayInfoEntity holidayInfo) {
        this.type = type;
        this.seHolidayInfo(holidayInfo);
    }

    public void seHolidayInfo(HolidayInfoEntity holidayInfo) {

        if(this.holidayInfo != null) {
            this.holidayInfo.getHolidayTypeEntityList().remove(this);
        }

        this.holidayInfo = holidayInfo;
        holidayInfo.getHolidayTypeEntityList().add(this);

    }

    public void updateType(String type) {
        this.type = type;
    }

}
