package com.fxservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// This is not an Entity, just a simple class to structure the API response.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversionResponse {

    private String fromCurrency;
    private String toCurrency;
    private BigDecimal originalAmount;
    private BigDecimal convertedAmount;
    private BigDecimal exchangeRate;

}
