/*
 * Copyright (c) 2024. SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cco.plugin;

import com.sap.cco.plugin.apis.ExchangeRateAPI;

public final class APIFactory {

    public static AbstractExchangeAPI exchangeRateAPI() {
        return new ExchangeRateAPI();
    }

}
