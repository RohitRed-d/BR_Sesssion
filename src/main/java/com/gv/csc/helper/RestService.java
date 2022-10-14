package com.gv.csc.helper;


import com.gv.csc.exceptions.PLMException;
import com.gv.csc.util.PLMConstants;
import okhttp3.*;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * RestService - Helper class containing the helper logic for speaking with Rest APIs calls and preparing response
 */
public class RestService {
    /**
     * make post or get calls
     *
     * @param url         server url
     * @param methodType  method type
     * @param headers     headers
     * @param requestBody data to be added
     * @return success response
     */
    public ResponseEntity<String> makeGetOrPostCall(String url,
                                                    HttpMethod methodType,
                                                    HttpHeaders headers,
                                                    JSONObject requestBody) {
        CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier())
                .build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);

        RestTemplate restTemplate = new RestTemplate(requestFactory);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        return restTemplate.exchange(url, methodType, entity, String.class);
    }
    
    /**
     * make post or get calls
     *
     * @param url         server url
     * @param methodType  method type
     * @param headers     headers
     * @param requestBody data to be added
     * @return success response
     */
    public ResponseEntity<String> doGetOrPostCall(String url,
                                                    HttpMethod methodType,
                                                    HttpHeaders headers,
                                                    String requestBody) {
        CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier())
                .build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);

        RestTemplate restTemplate = new RestTemplate(requestFactory);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        return restTemplate.exchange(url, methodType, entity, String.class);
    }

    /**
     *
     * @param multipartBody
     * @param url
     * @param headers
     * @return success response
     */
    public ResponseEntity<String> uploadMultipartFormData(MultiValueMap<String, Object> multipartBody,
                                                          String url,
                                                          HttpHeaders headers) {
        headers.remove(HttpHeaders.CONTENT_TYPE);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(multipartBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForEntity(url, requestEntity, String.class);
    }

    public Response okmakeGetOrPostCall(String url, JSONObject requestBody, String token) throws PLMException {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(480, TimeUnit.SECONDS);
        builder.readTimeout(480, TimeUnit.SECONDS);
        builder.writeTimeout(480, TimeUnit.SECONDS);
        OkHttpClient client = builder.build();

        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("requestxml", requestBody.toString())
                .build();
        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .addHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .addHeader("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE)
                .addHeader("t", token)
                .addHeader(PLMConstants.API_HEADER_VERSION_KEY, PLMConstants.API_HEADER_VERSION_VALUE)
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();

        } catch (IOException e) {
            e.printStackTrace();
            throw new PLMException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }
    /**
     * make patch calls
     *
     * @param url         server url
     * @param headers     headers
     * @param requestBody data to be updated with
     * @return success response data
     */
    public String makePatchCall(String url,
                                HttpHeaders headers,
                                JSONObject requestBody) {

        RestTemplate restTemplate = new RestTemplate();
        HttpClient httpClient = HttpClientBuilder.create().build();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, requestEntity, String.class);
        return response.getBody();
    }
}
