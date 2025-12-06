package com.planitsquare.holiday.constant;

import lombok.Getter;

@Getter
public enum HolidaySortType {

    LOCALNAME("localName"),
    NAME("name"),
    COUNTRYCODE("countryCode"),
    DATE("holidayDate");

    private final String sortColumn;

    HolidaySortType(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    public static HolidaySortType valueOfString(String value) {
        for (HolidaySortType type : HolidaySortType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
}
