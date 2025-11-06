package com.fxservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

// This annotation marks this class as a JPA entity, meaning it maps to a database table.
@Entity
// The table name in PostgreSQL will be 'exchange_rates'.
@Table(name = "exchange_rates")
public class ExchangeRate {

    // Primary key for the table, generated automatically by the database.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The currency the rate is based on (e.g., "USD").
    @Column(nullable = false, length = 3)
    private String baseCurrency;

    // The currency we are converting to (e.g., "EUR").
    @Column(nullable = false, length = 3)
    private String targetCurrency;

    // The actual exchange rate (e.g., 1 USD = 0.92 EUR). Using BigDecimal is crucial
    // for financial data to avoid floating-point errors.
    // FIX: Increased precision from 10 to 20 to safely store very large exchange rates (e.g., IRR).
    @Column(nullable = false, precision = 20, scale = 6)
    private BigDecimal rate;

    // The date the rate was fetched, allowing for historical auditing.
    @Column(nullable = false)
    private LocalDate date;

    // --- Constructor and Getters/Setters (Boilerplate) ---

    public ExchangeRate() {}

    public ExchangeRate(String baseCurrency, String targetCurrency, BigDecimal rate, LocalDate date) {
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
        this.date = date;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String baseCurrency) { this.baseCurrency = baseCurrency; }
    public String getTargetCurrency() { return targetCurrency; }
    public void setTargetCurrency(String targetCurrency) { this.targetCurrency = targetCurrency; }
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}