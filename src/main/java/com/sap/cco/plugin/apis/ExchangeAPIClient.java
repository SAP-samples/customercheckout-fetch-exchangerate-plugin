/*
 * Copyright (c) 2024. SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cco.plugin.apis;

import com.sap.scco.ap.pos.i14y.central.retrofit.RetrofitClient;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ExchangeAPIClient extends RetrofitClient {

    /**
     * Get all exchange rates
     */
    @GET("http://open.er-api.com/v6/latest/USD")
    Call<ExchangeAPIResponseDTO> getExchangeRates();
}
