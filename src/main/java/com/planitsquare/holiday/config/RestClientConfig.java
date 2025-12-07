package com.planitsquare.holiday.config;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;



@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient() {
        // Connection Pool 생성 (가장 간단한 방식)
        PoolingHttpClientConnectionManager connectionManager =
                new PoolingHttpClientConnectionManager();

        // Connection Pool 설정
        connectionManager.setMaxTotal(100);              // 전체 최대 연결 수
        connectionManager.setDefaultMaxPerRoute(100);     // 호스트당 최대 연결 수

        // HttpClient 생성
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        // HttpComponentsClientHttpRequestFactory 생성
        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        // 타임아웃 설정 (밀리초 단위)
        factory.setConnectTimeout(5000);                 // 연결 타임아웃: 5초
        factory.setConnectionRequestTimeout(5000);       // Connection Pool에서 연결 가져오기 타임아웃: 3초

        // RestClient 생성
        return RestClient.builder()
                .requestFactory(factory)
                .baseUrl("https://date.nager.at/api/")
                .defaultHeaders(headers -> headers.setContentType(MediaType.APPLICATION_JSON))
                .build();
    }

//    @Bean
//    public RestClient createRestClient(CloseableHttpClient httpClient){
//        return RestClient
//                .builder()
//                .requestFactory(requestFactory)
//                .baseUrl("https://date.nager.at/api/")
//                .defaultHeaders(headers -> headers.setContentType(MediaType.APPLICATION_JSON))
//                .build();
//    }
}
