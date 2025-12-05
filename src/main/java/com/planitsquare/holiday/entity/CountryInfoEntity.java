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
@Table(name="CountryInfo")
public class CountryInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long countryInfoSeq;

    @NotNull
    @Column
    String name;

    @NotNull
    @Column
    String countryCode;

}
