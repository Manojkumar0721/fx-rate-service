package com.fxservice.controller;

import com.fxservice.dto.ConversionResponse;
import com.fxservice.service.ExchangeRateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/fx") // Base path for all FX endpoints (e.g., http://localhost:8080/api/fx/...)
// FIX: Add CrossOrigin annotation to allow the frontend HTML file to access this API.
@CrossOrigin(origins = "*")
public class FxRateController {

    private final ExchangeRateService exchangeRateService;

    // Dependency Injection
    public FxRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    /**
     * Endpoint to manually trigger the daily rate fetching and saving job.
     * GET /api/fx/latest
     */
    @GetMapping("/latest")
    public ResponseEntity<String> fetchLatestRates() {
        // Calls the same method used by the @Scheduled job
        exchangeRateService.fetchAndSaveDailyRates();
        return ResponseEntity.ok("Rates fetching initiated and saved successfully! Check your PostgreSQL log.");
    }

    /**
     * Endpoint to perform a currency conversion calculation.
     * GET /api/fx/convert?from=USD&to=EUR&amount=100
     *
     * @param from The currency to convert from (e.g., USD)
     * @param to The currency to convert to (e.g., EUR)
     * @param amount The amount to convert
     * @return ConversionResponse DTO containing the result
     */
    @GetMapping("/convert")
    public ResponseEntity<ConversionResponse> convert(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal amount) {

        // The service layer handles the lookup and calculation
        ConversionResponse response = exchangeRateService.convertCurrency(from, to, amount);
        return ResponseEntity.ok(response);
    }
}