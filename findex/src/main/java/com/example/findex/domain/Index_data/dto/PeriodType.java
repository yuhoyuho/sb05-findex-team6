package com.example.findex.domain.Index_data.dto;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PeriodType {
    YEARLY,
    QUARTERLY,
    MONTHLY;

    @JsonCreator
    public static PeriodType fromString(String value) {
        return PeriodType.valueOf(value.toUpperCase());
    }
}
