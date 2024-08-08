/*
 * Copyright (c) 2024. SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cco.plugin.apis;

import com.sap.cco.plugin.AbstractExchangeAPI;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Credits <a href="https://www.exchangerate-api.com">Rates By Exchange Rate API</a>
 */
public class ExchangeRateAPI extends AbstractExchangeAPI {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateAPI.class);

    protected final ExchangeRateAPICommunicator communicator;

    public ExchangeRateAPI() {
        this(new ExchangeRateAPICommunicator());
    }

    public ExchangeRateAPI(ExchangeRateAPICommunicator communicator) {
        this.communicator = communicator;
    }

    @Override
    protected BigDecimal exchangeRateInRelationToUSD(String requestCurrency) {
        requestCurrency = requestCurrency.toUpperCase();
        try {
            Map<String, BigDecimal> allRates = communicator.request().body().getRates();
            conversionRates.putAll(allRates);
            return conversionRates.get(requestCurrency);
        } catch (IOException e) {
            logger.warn("Error when retrieving currency", e);
            return BigDecimal.ZERO;
        }

    }
}
