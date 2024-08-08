/*
 * Copyright (c) 2024. SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cco.plugin;

import com.sap.scco.ap.plugin.BasePlugin;
import com.sap.scco.ap.plugin.PluginConfigurationDTO;
import com.sap.scco.ap.plugin.PluginConfigurationType;
import com.sap.scco.ap.plugin.annotation.Schedulable;
import com.sap.scco.ap.pos.dao.CDBSession;
import com.sap.scco.ap.pos.dao.CDBSessionFactory;
import com.sap.scco.ap.pos.dao.IPluginPropertyManager;
import com.sap.scco.ap.pos.dao.PluginPropertyManager;
import com.sap.scco.ap.pos.entity.ExchangeRatesEntity;
import com.sap.scco.ap.pos.entity.ExchangeRatesEntity.Scale;
import com.sap.scco.ap.pos.i14y.util.context.I14YContext;
import com.sap.scco.ap.pos.job.BaseJob.SynchronizationState;
import com.sap.scco.ap.pos.job.PluginJob;
import com.sap.scco.ap.pos.util.TriggerParameter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fetches the live exchange rates through an API
 */
public class FetchExchangeRatePlugin extends BasePlugin {

    private static final Logger logger = LoggerFactory.getLogger(FetchExchangeRatePlugin.class);
    private AbstractExchangeAPI api;

    @Override
    public String getId() {
        return "FetchExchangeRatePlugin";
    }

    @Override
    public String getName() {
        return "Fetch Exchange Rate Plugin";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    /**
     * Executed, when the plugin is started at the startup of the CCO
     */
    @Override
    public void startup() {
        saveDefaultProperty("enabled", String.valueOf(true));

        api = APIFactory.exchangeRateAPI();

        logger.info("Startup complete");
    }

    /**
     * Is called when the plugin config is opened.
     *
     * @return the configuration to display
     */
    @Override
    public List<PluginConfigurationDTO> getPluginPropertyConfiguration() {
        List<PluginConfigurationDTO> properties = new ArrayList<>();
        try (CDBSession dbSession = CDBSessionFactory.instance.createSession()) {
            for (ExchangeRatesEntity r : getActiveExchangeRates(dbSession)) {
                String id = exchangeId(r);
                properties.add(new PluginConfigurationDTO(id, "Offset " + id, PluginConfigurationType.DOUBLE));
                saveDefaultProperty(id, String.valueOf(0.0));
            }
        }
        return properties;
    }

    private String exchangeId(ExchangeRatesEntity r) {
        return r.getSourceCurrency().getCurrencyCode() + "->" + r.getTargetCurrency().getCurrencyCode();
    }

    /**
     * The plugin is set to save the settings to the database.
     *
     * @return true
     */
    @Override
    public boolean persistPropertiesToDB() {
        return true;
    }


    /**
     * Saves the default value for a property iff the property currently has no value assigned.
     *
     * @param key key
     * @param defaultValue default value
     */
    private void saveDefaultProperty(String key, String defaultValue) {
        String propertyValue = getProperty(key, "");
        if (propertyValue.isEmpty()) {
            overwriteProperty(key, defaultValue);
        }
    }

    /**
     * Overwrites the property of key with value, replacing the old value.
     *
     * @param key key
     * @param value new value
     * @return old value
     */
    private Object overwriteProperty(String key, String value) {
        Properties properties = getProperties();
        Object old = properties.setProperty(key, value);
        setProperties(properties);

        try (CDBSession dbSession = CDBSessionFactory.instance.createSession()) {
            IPluginPropertyManager propertyManager = new PluginPropertyManager(dbSession);
            propertyManager.setPluginProperties(this, properties);
        }
        return old;
    }


    /**
     * Retrieve all currencies to update using the plugin settings, which are adapted to the "payment supported" currencies in the currencies screen
     *
     * @param job job
     * @param triggerParam triggerParam
     * @param context context
     * @param params params
     */
    @Schedulable("Fetch currency exchange rates")
    public void fetchCurrencyExchangeRates(PluginJob job, TriggerParameter triggerParam, I14YContext context, Object[] params) {
        try (CDBSession dbSession = CDBSessionFactory.instance.createSession()) {
            try {
                dbSession.beginTransaction();

                List<ExchangeRatesEntity> exchangeRates = getActiveExchangeRates(dbSession);

                for (ExchangeRatesEntity r : exchangeRates) {
                    setExchangeRateFor(job, r, dbSession);
                }

                if (dbSession.isTransactionActive()) {
                    dbSession.commitTransaction();
                }
            } catch (Exception e) {
                logger.error("Exchange rate update failed", e);
                dbSession.rollbackDBSession();
            }
        }
    }

    private void setExchangeRateFor(PluginJob job, ExchangeRatesEntity r, CDBSession dbSession) {
        try {
            BigDecimal offset = BigDecimal.valueOf(getProperty(exchangeId(r), 1.0));
            BigDecimal rate = api.exchangeRateFor(r.getSourceCurrency().getCurrencyCode(), r.getTargetCurrency().getCurrencyCode());

            rate = rate.add(offset).setScale(Scale.DEFAULT, RoundingMode.HALF_UP);

            if (rate.signum() == 1) {
                r.setMiddleRate(rate);
            } else {
                String message = "Exchange rate for " + exchangeId(r) + " would be negative (" + rate + ") and thus wasn't set";
                job.synchronizationLogging(message, SynchronizationState.GENERAL_WARNING);
                logger.info(message);
            }

            logger.info("{} -> {} = {}", r.getSourceCurrency().getCurrencyCode(), r.getTargetCurrency().getCurrencyCode(), rate);
            dbSession.getEM().persist(r);
        } catch (NoChangeException ignored) {
            logger.info("Couldn't calculate exchange rate for {} -> {}. Skipping.", r.getSourceCurrency().getCurrencyCode(), r.getTargetCurrency().getCurrencyCode());
        }
    }

    private List<ExchangeRatesEntity> getActiveExchangeRates(CDBSession dbSession) {
        List<ExchangeRatesEntity> exchangeRates = dbSession.getEM()
            .createQuery("SELECT r from ExchangeRatesEntity r where r.isActive = TRUE", ExchangeRatesEntity.class)
            .getResultList();
        return exchangeRates;
    }


}
