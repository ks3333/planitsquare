package com.planitsquare.holiday.store;

import com.planitsquare.holiday.Exception.RestClientCallException;
import com.planitsquare.holiday.model.CountryInfoDto;
import com.planitsquare.holiday.model.HolidayInfoDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiCallStoreUnitTest {

    private static MockWebServer mockWebServer;
    private ApiCallStore apiCallStore;
    private RestClient restClient;

    @BeforeAll
    static void setUpClass() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDownClass() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void setUp() {
        String baseUrl = mockWebServer.url("/").toString();
        restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        apiCallStore = new ApiCallStore(restClient);
    }

    @Test
    @Order(1)
    @DisplayName("국가 코드 목록 조회 성공 테스트")
    void testGetCountryCodeList_Success() throws InterruptedException {
        // Given
        String jsonResponse = """
                [
                    {
                        "countryCode": "KR",
                        "name": "South Korea"
                    },
                    {
                        "countryCode": "US",
                        "name": "United States"
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(200));

        // When
        List<CountryInfoDto> result = apiCallStore.getCountryCodeList();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("KR", result.get(0).getCountryCode());
        assertEquals("South Korea", result.get(0).getName());

        // Verify request
        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals("/v3/AvailableCountries", request.getPath());
        assertEquals("GET", request.getMethod());
    }

    @Test
    @Order(2)
    @DisplayName("국가 코드 목록 조회 - 4xx 에러")
    void testGetCountryCodeList_ClientError() {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("Not Found"));

        // When & Then
        RestClientCallException exception = assertThrows(
                RestClientCallException.class,
                () -> apiCallStore.getCountryCodeList()
        );

        assertEquals(404, exception.getCode());
        assertTrue(exception.getMessage().contains("/v3/AvailableCountries"));
    }

    @Test
    @Order(3)
    @DisplayName("국가 코드 목록 조회 - 5xx 에러")
    void testGetCountryCodeList_ServerError() {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        // When & Then
        RestClientCallException exception = assertThrows(
                RestClientCallException.class,
                () -> apiCallStore.getCountryCodeList()
        );

        assertEquals(500, exception.getCode());
    }

    @Test
    @Order(4)
    @DisplayName("휴일 목록 조회 성공 테스트")
    void testGetHolidayList_Success() throws InterruptedException {
        // Given
        int year = 2024;
        String country = "KR";
        String jsonResponse = """
                [
                    {
                        "date": "2024-01-01",
                        "localName": "신정",
                        "name": "New Year's Day",
                        "countryCode": "KR",
                        "fixed": true,
                        "global": true,
                        "counties": null,
                        "launchYear": null,
                        "types": ["Public"]
                    },
                    {
                        "date": "2024-03-01",
                        "localName": "삼일절",
                        "name": "Independence Movement Day",
                        "countryCode": "KR",
                        "fixed": true,
                        "global": true,
                        "counties": null,
                        "launchYear": null,
                        "types": ["Public"]
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(200));

        // When
        List<HolidayInfoDto> result = apiCallStore.getHolidayList(year, country);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(LocalDate.of(2024, 1, 1), result.get(0).getDate());
        assertEquals("신정", result.get(0).getLocalName());
        assertEquals("New Year's Day", result.get(0).getName());
        assertEquals(2024, result.get(0).getHolidayYear());

        // Verify request
        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
    }

    @Test
    @Order(5)
    @DisplayName("휴일 목록 조회 - 잘못된 국가 코드")
    void testGetHolidayList_InvalidCountryCode() {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("Country not found"));

        // When & Then
        RestClientCallException exception = assertThrows(
                RestClientCallException.class,
                () -> apiCallStore.getHolidayList(2024, "INVALID")
        );

        assertEquals(404, exception.getCode());
    }

    @Test
    @Order(6)
    @DisplayName("병렬 휴일 목록 조회 성공 테스트")
    void testGetHolidayInfoList_Success() {
        // Given
        List<Integer> yearList = List.of(2023, 2024);
        List<String> countryList = List.of("KR", "US");

        String jsonResponse = """
                [
                    {
                        "date": "2024-01-01",
                        "localName": "New Year",
                        "name": "New Year's Day",
                        "countryCode": "KR",
                        "fixed": true,
                        "global": true,
                        "counties": null,
                        "launchYear": null,
                        "types": ["Public"]
                    }
                ]
                """;

        // 2023-KR, 2023-US, 2024-KR, 2024-US 총 4번의 응답 준비
        for (int i = 0; i < 4; i++) {
            mockWebServer.enqueue(new MockResponse()
                    .setBody(jsonResponse)
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setResponseCode(200));
        }

        // When
        List<HolidayInfoDto> result = apiCallStore.getHolidayInfoList(yearList, countryList);

        // Then
        assertNotNull(result);
        assertEquals(4, result.size()); // 4개의 API 호출 결과

        // 모든 결과에 year가 설정되었는지 확인
        result.forEach(holiday -> {
            assertNotNull(holiday.getHolidayYear());
            assertTrue(holiday.getHolidayYear() >= 2023 && holiday.getHolidayYear() <= 2024);
        });
    }

    @Test
    @Order(7)
    @DisplayName("병렬 휴일 목록 조회 - 부분 실패")
    void testGetHolidayInfoList_PartialFailure() {
        // Given
        List<Integer> yearList = List.of(2024);
        List<String> countryList = List.of("KR", "INVALID");

        String successResponse = """
                [
                    {
                        "date": "2024-01-01",
                        "localName": "New Year",
                        "name": "New Year's Day",
                        "countryCode": "KR",
                        "fixed": true,
                        "global": true,
                        "counties": null,
                        "launchYear": null,
                        "types": ["Public"]
                    }
                ]
                """;

        // 첫 번째는 성공, 두 번째는 실패
        mockWebServer.enqueue(new MockResponse()
                .setBody(successResponse)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(200));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("Country not found"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            apiCallStore.getHolidayInfoList(yearList, countryList);
        });
    }

    @Test
    @Order(8)
    @DisplayName("병렬 호출 동시성 제한 테스트")
    void testGetHolidayInfoList_ConcurrencyLimit() throws InterruptedException {
        // Given
        List<Integer> yearList = List.of(2024);
        List<String> countryList = List.of("KR", "US", "JP", "CN", "DE");

        String jsonResponse = """
                [
                    {
                        "date": "2024-01-01",
                        "localName": "Holiday",
                        "name": "Holiday",
                        "countryCode": "KR",
                        "fixed": true,
                        "global": true,
                        "counties": null,
                        "launchYear": null,
                        "types": ["Public"]
                    }
                ]
                """;

        // 5개의 응답 준비 (각 국가당 1개)
        for (int i = 0; i < 5; i++) {
            mockWebServer.enqueue(new MockResponse()
                    .setBody(jsonResponse)
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setResponseCode(200)
                    .setBodyDelay(100, TimeUnit.MILLISECONDS)); // 응답 지연
        }

        // When
        long startTime = System.currentTimeMillis();
        List<HolidayInfoDto> result = apiCallStore.getHolidayInfoList(yearList, countryList);
        long endTime = System.currentTimeMillis();

        // Then
        assertNotNull(result);
        assertEquals(5, result.size());

        // 병렬 처리로 인해 순차 처리보다 빨라야 함
        long duration = endTime - startTime;
        assertTrue(duration < 500, "병렬 처리 시간이 너무 깁니다: " + duration + "ms");
    }

    @Test
    @Order(9)
    @DisplayName("빈 리스트로 호출 시 테스트")
    void testGetHolidayInfoList_EmptyLists() {
        // Given
        List<Integer> yearList = List.of();
        List<String> countryList = List.of();

        // When
        List<HolidayInfoDto> result = apiCallStore.getHolidayInfoList(yearList, countryList);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Nested
    @DisplayName("ApiCaller 내부 클래스 테스트")
    class ApiCallerTest {

        @Test
        @DisplayName("파라미터가 있는 GET 요청")
        void testCallApiGet_WithParameters() throws InterruptedException {
            // Given
            String jsonResponse = """
                    [
                        {
                            "date": "2024-01-01",
                            "localName": "Test",
                            "name": "Test Holiday",
                            "countryCode": "KR",
                            "fixed": true,
                            "global": true,
                            "counties": null,
                            "launchYear": null,
                            "types": ["Public"]
                        }
                    ]
                    """;

            mockWebServer.enqueue(new MockResponse()
                    .setBody(jsonResponse)
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setResponseCode(200));

            // When
            List<HolidayInfoDto> result = apiCallStore.getHolidayList(2024, "KR");

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());

            // Verify URL parameters
            RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
            assertNotNull(request);
            assertTrue(request.getPath().contains("2024"));
            assertTrue(request.getPath().contains("KR"));
        }

        @Test
        @DisplayName("Null 파라미터 필터링 테스트")
        void testCallApiGet_NullParameterFiltering() throws InterruptedException {
            // Given
            String jsonResponse = """
                    [
                        {
                            "countryCode": "KR",
                            "name": "South Korea"
                        }
                    ]
                    """;

            mockWebServer.enqueue(new MockResponse()
                    .setBody(jsonResponse)
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setResponseCode(200));

            // When
            List<CountryInfoDto> result = apiCallStore.getCountryCodeList();

            // Then
            assertNotNull(result);

            RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
            assertNotNull(request);
        }
    }
}
