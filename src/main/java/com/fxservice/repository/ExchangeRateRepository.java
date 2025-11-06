package com.fxservice.repository;

import com.fxservice.model.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    /**
     * Finds the single latest (newest date) exchange rate for a specific pair.
     * Spring Data JPA creates the SQL query automatically based on this method name.
     * * The method name uses 'BaseCurrency' to match the field in ExchangeRate.java.
     */
    Optional<ExchangeRate> findTopByBaseCurrencyAndTargetCurrencyOrderByDateDesc(String baseCurrency, String targetCurrency);
}