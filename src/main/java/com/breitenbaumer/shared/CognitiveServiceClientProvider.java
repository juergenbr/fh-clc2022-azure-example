package com.breitenbaumer.shared;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.util.UrlBuilder;

public class CognitiveServiceClientProvider {
    private Logger logger;
    private String endpoint = "westeurope.api.cognitive.microsoft.com";
    private String subscriptionKeyEnvVarName = "SUBSCRIPTIONKEY";
    private String endpointPath = "vision/v3.2/analyze";

    public CognitiveServiceClientProvider(Logger logger) {
        this.logger = logger;
    }

    public void sendRequest(String urlToImage) {
        HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

        try {
            UrlBuilder builder = new UrlBuilder().setHost(endpoint);
            builder.setPath(endpointPath);

            HttpRequest request = new HttpRequest(HttpMethod.POST, builder.toUrl());

            // Request headers.
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", System.getenv(subscriptionKeyEnvVarName));

            request.setBody("{\"url\":\"" + urlToImage + "\"}");

            // Call the REST API method and get the response entity.
            HttpResponse response = httpClient.send(request).block();
            if(response.getStatusCode() >= 200 && response.getStatusCode() < 300){
                logger.log(Level.INFO, "Request successful with status code " + response.getStatusCode());
                logger.log(Level.INFO, "Response body: " + response.getBodyAsString().block());
            }
            else{
                logger.log(Level.WARNING, "Request NOT successful with status code " + response.getStatusCode());
                logger.log(Level.WARNING, "Response body: " + response.getBodyAsString().block());
            }
        } catch (Exception e) {
            // Display error message.
            System.out.println(e.getMessage());
        }
    }
}