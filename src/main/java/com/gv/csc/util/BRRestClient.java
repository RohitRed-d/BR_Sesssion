package com.gv.csc.util;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service("BRRestClient")
public class BRRestClient {

    private final RestTemplate restTemplate;

    BRRestClient() {
        CloseableHttpClient httpClient = HttpClients
                .custom()
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);

        this.restTemplate = new RestTemplate(requestFactory);
    }

    public ResponseEntity<String> makeRESTCall(String url,
                                               HttpMethod methodType,
                                               HttpHeaders headers,
                                               JSONObject requestBody) {

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        return restTemplate.exchange(url, methodType, entity, String.class);
    }

}
