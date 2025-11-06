package com.fxservice.service;

import com.fxservice.dto.ConversionResponse;
import com.fxservice.model.ExchangeRate;
import com.fxservice.repository.ExchangeRateRepository;
import com.fxservice.exception.RateNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Service
public class ExchangeRateService {

    private static final String BASE_CURRENCY = "EUR"; // Fixed base for Frankfurter API
    private final ExchangeRateRepository rateRepository;
    private final WebClient frankfurterWebClient;

    @Autowired
    public ExchangeRateService(ExchangeRateRepository rateRepository, WebClient frankfurterWebClient) {
        this.rateRepository = rateRepository;
        this.frankfurterWebClient = frankfurterWebClient;
    }

    /**
     * Finds the latest rate for a specific currency pair (A -> B).
     */
    private Optional<ExchangeRate> findLatestRate(String base, String target) {
        // Uses the correct method name based on the repository interface
        return rateRepository.findTopByBaseCurrencyAndTargetCurrencyOrderByDateDesc(base, target);
    }

    /**
     * Scheduled job to fetch and save daily exchange rates.
     * Runs every 15 minutes for demonstration purposes.
     */
    // @Scheduled(cron = "0 0 0 * * *") // Daily at midnight for production
    @Scheduled(fixedRate = 900000) // Every 15 minutes (for testing/demo)
    @Transactional
    public void fetchAndSaveDailyRates() {
        System.out.println("--- Starting Scheduled Rate Fetch Job ---");
        try {
            // 1. Call the external API to get the latest rates, base=EUR
            Map<String, Object> response = frankfurterWebClient.get()
                    .uri("/latest?from={base}", BASE_CURRENCY)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || !response.containsKey("rates")) {
                System.err.println("API response missing 'rates' map.");
                return;
            }

            // 2. Extract date and rates map
            Map<String, Number> ratesMap = (Map<String, Number>) response.get("rates");
            String dateString = (String) response.get("date");
            LocalDate date = LocalDate.parse(dateString);

            // 3. Save each target rate to the database (Universal Number Fix)
            for (Map.Entry<String, Number> entry : ratesMap.entrySet()) {
                String target = entry.getKey();

                // CRITICAL FIX: Get value as a generic Number, convert to Double, then to BigDecimal
                BigDecimal rateValue = BigDecimal.valueOf(entry.getValue().doubleValue());

                ExchangeRate rate = new ExchangeRate(BASE_CURRENCY, target, rateValue, date);
                rateRepository.save(rate);
                System.out.printf("Saved rate: 1 %s = %s %s on %s%n", BASE_CURRENCY, rateValue, target, date);
            }

            // 4. Also save the base-to-base rate (EUR to EUR) as 1.0
            rateRepository.save(new ExchangeRate(BASE_CURRENCY, BASE_CURRENCY, BigDecimal.ONE, date));
            System.out.println("Saved base-to-base rate: 1 EUR = 1 EUR.");


            System.out.println("--- Scheduled Rate Fetch Job Finished Successfully ---");

        } catch (Exception e) {
            System.err.println("Error during scheduled rate fetch: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * CORE FIX: Performs the currency conversion calculation.
     * Uses EUR as the pivot currency (Rate_FROM_to_TO = Rate_EUR_to_TO / Rate_EUR_to_FROM).
     */
    @Transactional(readOnly = true)
    public ConversionResponse convertCurrency(String from, String to, BigDecimal amount) {

        // 1. Handle same currency conversion (e.g., USD to USD)
        if (from.equalsIgnoreCase(to)) {
            // The exchange rate is 1.0, and the amount remains the same.
            return new ConversionResponse(from, to, amount, amount, BigDecimal.ONE.setScale(6, RoundingMode.HALF_UP));
        }

        // 2. Get the rate of the 'from' currency relative to the Base (EUR -> FROM)
        // If from=USD, this returns the rate for 1 EUR to USD.
        BigDecimal rateFrom;
        try {
            rateFrom = getRateToBase(from);
        } catch (RateNotFoundException e) {
            // Re-throw if rate is genuinely missing
            throw new RateNotFoundException("Rate not found for source currency: " + from);
        }


        // 3. Get the rate of the 'to' currency relative to the Base (EUR -> TO)
        // If to=GBP, this returns the rate for 1 EUR to GBP.
        BigDecimal rateTo;
        try {
            rateTo = getRateToBase(to);
        } catch (RateNotFoundException e) {
            // Re-throw if rate is genuinely missing
            throw new RateNotFoundException("Rate not found for target currency: " + to);
        }

        // 4. Calculate the cross-rate (Rate: FROM -> TO)
        // Formula: Rate_FROM_to_TO = Rate_EUR_to_TO / Rate_EUR_to_FROM
        // Precision is set to 6 decimal places for the rate.
        BigDecimal crossRate = rateTo.divide(rateFrom, 6, RoundingMode.HALF_UP);

        // 5. Calculate the final converted amount
        BigDecimal convertedAmount = amount.multiply(crossRate).setScale(2, RoundingMode.HALF_UP);

        // 6. Return the response DTO
        return new ConversionResponse(from, to, amount, convertedAmount, crossRate);
    }

    /**
     * Internal helper to retrieve the rate of a given currency against the fixed Base (EUR).
     * Since the database stores EUR->X rates, this method returns the rate of X
     * in terms of the EUR base.
     * @param currency The currency code (e.g., "USD")
     * @return The rate for 1 EUR = X currency.
     */
    private BigDecimal getRateToBase(String currency) {
        // If the currency IS the base (EUR), the rate is 1.00
        if (currency.equalsIgnoreCase(BASE_CURRENCY)) {
            return BigDecimal.ONE;
        }

        // Try to find the rate from the fixed base (EUR -> Currency)
        Optional<ExchangeRate> rateOptional = findLatestRate(BASE_CURRENCY, currency);

        if (rateOptional.isPresent()) {
            // Found EUR -> Currency rate
            return rateOptional.get().getRate();
        }

        // If we reach here, the currency is genuinely missing from the last fetch.
        // We throw the custom exception which the controller maps to an HTTP 404.
        throw new RateNotFoundException("Rate for " + currency + " (relative to " + BASE_CURRENCY + ") not found in database. Please check currency code or run the initial fetch job.");
    }
}