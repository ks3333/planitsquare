package com.planitsquare.holiday.entity;

import com.planitsquare.holiday.model.CountryInfoDto;
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
@Table(name="countryInfo")
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

    public CountryInfoDto makeDto() {
        return CountryInfoDto.builder()
                .countryInfoSeq(this.countryInfoSeq)
                .name(this.name)
                .countryCode(this.countryCode)
                .build();
    }
}
