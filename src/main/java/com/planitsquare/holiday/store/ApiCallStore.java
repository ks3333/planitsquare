package com.planitsquare.holiday.store;

import com.planitsquare.holiday.Exception.RestClientCallException;
import com.planitsquare.holiday.model.CountryInfoDto;
import com.planitsquare.holiday.model.HolidayInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.HttpStatusCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@Component
@Slf4j
public class ApiCallStore {

    private final RestClient client;

    public ApiCallStore(RestClient client) {
        this.client = client;
    }

    /**
     * 국가 코드 리스트 조회 API 호출
     * @return 국가 코드 리스트
     */
    public List<CountryInfoDto> getCountryCodeList(){
        ParameterizedTypeReference<List<CountryInfoDto>> responseType = new ParameterizedTypeReference<List<CountryInfoDto>>() {};
        ApiCaller<List<CountryInfoDto>> caller = new ApiCaller<List<CountryInfoDto>>(client, responseType);
        return caller.callApiGet("/v3/AvailableCountries");
    }

    /**
     * 특정 연도, 국가의 공휴일 리스트 조회 API 호출
     * @param year 연도
     * @param country 국가
     * @return 선택한 국가의 선택연도 공휴일 리스트
     */
    public List<HolidayInfoDto> getHolidayList(int year, String country){
        ParameterizedTypeReference<List<HolidayInfoDto>> responseType = new ParameterizedTypeReference<List<HolidayInfoDto>>() {};
        ApiCaller<List<HolidayInfoDto>> caller = new ApiCaller<List<HolidayInfoDto>>(client, responseType);
        List<HolidayInfoDto> result =  caller.callApiGet("/v3/PublicHolidays/{year}/{country}", year, country);
        result.forEach(f -> f.setHolidayYear(year));
        return listDistinctByKey(result);
    }

    /**
     * 여러 연도, 국가의 공휴일 리스트 조회 API 호출 (동시 호출처리)
     * @param yearList 연도 목록
     * @param countryList 국가 목록
     * @return 선택한 국가들의 선택연도 공휴일 리스트
     */
    public List<HolidayInfoDto> getHolidayInfoList(List<Integer> yearList, List<String> countryList){
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        List<CompletableFuture<List<HolidayInfoDto>>> futures = new ArrayList<>();

        int maxConcurrency = 20; // 동시 수행 제한
        Semaphore semaphore = new Semaphore(maxConcurrency);

        for (Integer year : yearList) {
            for (String country : countryList) {

                CompletableFuture<List<HolidayInfoDto>> future =
                        CompletableFuture.supplyAsync(
                                () -> {
                                    try {
                                        semaphore.acquire();
                                        return getHolidayList(year, country);
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    } finally {
                                        semaphore.release();
                                    }
                                },
                                executor
                        );
                futures.add(future);
            }
        }

        // 모든 비동기 작업이 끝날 때까지 기다리고 결과 수집
        List<List<HolidayInfoDto>> results = futures.stream()
                .map(CompletableFuture::join)  // join 시 예외는 CompletionException으로 포장됨
                .toList();

        executor.shutdown();

        // flatten list
        return results.stream()
                .flatMap(List::stream)
                .distinct()
                .toList();
    }

    /**
     * 리스트 내 중복 데이터 제거
     * @param list 공휴일 목록
     * @return 중복 제거된 공휴일 목록(국가/날자/공휴일 명 기준 중복제거)
     */
    public List<HolidayInfoDto> listDistinctByKey(List<HolidayInfoDto> list) {
        return list.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    static class ApiCaller<T> {

        private final RestClient client;

        private final ParameterizedTypeReference<T> responseType;

        private String errorMsgTemplate = "API 호출 중 오류 발생 - url : %s, param : %s";
        public ApiCaller(RestClient client, ParameterizedTypeReference<T> responseType) {

            this.client = client;
            this.responseType = responseType;
        }

        public T callApiGet(String url, Object ... param) {

            Object[] parameter = Arrays.stream(param).filter(Objects::nonNull).toArray();

            try{
                return client.get().uri(url, param)
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                            String paramStr = Arrays.stream(parameter).map(Object::toString).reduce((a, b) -> a + ", " + b).orElse("");
                            throw new RestClientCallException(errorMsgTemplate.formatted(url, paramStr), response.getStatusCode().value(), response.getStatusText());
                        })
                        .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                            String paramStr = Arrays.stream(parameter).map(Object::toString).reduce((a, b) -> a + ", " + b).orElse("");
                            throw new RestClientCallException(errorMsgTemplate.formatted(url, paramStr), response.getStatusCode().value(), response.getStatusText());
                        })
                        .body(responseType);
            } catch (RestClientCallException e){
                throw e;
            } catch (Exception e){
                String paramStr = Arrays.stream(parameter).map(Object::toString).reduce((a, b) -> a + ", " + b).orElse("");
                throw new RestClientCallException(errorMsgTemplate.formatted(url, paramStr), -1, e.getMessage());
            }
        }

    }
}
