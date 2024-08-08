/*
 * Copyright (c) 2024. SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cco.plugin.apis;

import com.sap.cco.plugin.AbstractCommunicator;
import java.io.IOException;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class ExchangeRateAPICommunicator extends AbstractCommunicator {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateAPICommunicator.class);

    protected ExchangeAPIClient con;

    public ExchangeRateAPICommunicator() {
        OkHttpClient client = new OkHttpClient.Builder()
            .build();

        Retrofit retrofit = new Retrofit.Builder()
            .client(client)
            .baseUrl("http://localhost")
            .addConverterFactory(JacksonConverterFactory.create())
            .build();

        this.con = retrofit.create(ExchangeAPIClient.class);
    }

    public Response<ExchangeAPIResponseDTO> request() throws IOException {
        Response<ExchangeAPIResponseDTO> response = con.getExchangeRates().execute();
        if (response.body() == null) {
            throw new IOException("Response body was null");
        }
        return response;
    }

}
