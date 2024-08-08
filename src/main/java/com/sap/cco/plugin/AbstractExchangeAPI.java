/*
 * Copyright (c) 2024. SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cco.plugin;

import com.sap.scco.ap.pos.entity.ExchangeRatesEntity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines the minimum supported endpoints for an exchange api. We assume USD to be the base currency. This service is caching the responses automatically.
 */
public abstract class AbstractExchangeAPI {

    protected Map<String, BigDecimal> conversionRates = new HashMap<>();

    /**
     * Requests the exchange rate for the request currency in relation to baseCurrency
     * <br><br>
     * Example: (USD, EUR) = 0.94 means you get 0.94 EUR for 1 USD
     * <br>
     * Example: (CAD, EUR) = ???. This internally computes 1 USD = ? CAD and then computes 1 USD = ? EUR.
     * <br>
     * Let's say 1 USD = 1.37 CAD and 1 USD = 0.94 EUR. Then we compute 1 CAD = 1 / 1.37 USD = (1 / 1.37) * 0.94 EUR
     *
     * @param baseCurrency the base currency
     * @param requestCurrency the requested currency
     * @throws NoChangeException When we don't have enough information to calculate the exchange rate
     * @return exchange rate
     */
    public final BigDecimal exchangeRateFor(String baseCurrency, String requestCurrency) throws NoChangeException {
        baseCurrency = baseCurrency.toUpperCase();
        requestCurrency = requestCurrency.toUpperCase();
        if (!conversionRates.containsKey(baseCurrency)) {
            conversionRates.put(baseCurrency, exchangeRateInRelationToUSD(baseCurrency));
        }
        if (!conversionRates.containsKey(requestCurrency)) {
            conversionRates.put(requestCurrency, exchangeRateInRelationToUSD(requestCurrency));
        }
        BigDecimal baseIRUSD = conversionRates.get(baseCurrency);
        BigDecimal requestIRUSD = conversionRates.get(requestCurrency);

        if (null == baseIRUSD || null == requestIRUSD || baseIRUSD.signum() != 1 || requestIRUSD.signum() != 1) {
            throw new NoChangeException();
        }

        return requestIRUSD.divide(baseIRUSD, ExchangeRatesEntity.Scale.DEFAULT, RoundingMode.HALF_UP);
    }

    protected abstract BigDecimal exchangeRateInRelationToUSD(String requestCurrency);

}
