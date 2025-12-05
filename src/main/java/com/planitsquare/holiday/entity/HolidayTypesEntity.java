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
@Table(name="holidayType")
public class HolidayTypesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long holidayTypesSeq;

    @Column(insertable=false, updatable=false)
    Long holidayInfoSeq;

    @Column
    String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "holidayInfoSeq")
    HolidayInfoEntity holidayInfo;

}
