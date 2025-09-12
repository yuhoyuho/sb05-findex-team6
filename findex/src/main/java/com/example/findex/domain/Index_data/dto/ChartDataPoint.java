package com.example.findex.domain.Index_data.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataPoint {
    private LocalDate date;
    private double value;
}
