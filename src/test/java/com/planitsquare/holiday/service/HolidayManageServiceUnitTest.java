package com.planitsquare.holiday.service;

import com.planitsquare.holiday.Exception.DateNotExistException;
import com.planitsquare.holiday.Exception.RestClientCallException;
import com.planitsquare.holiday.constant.HolidaySortType;
import com.planitsquare.holiday.model.*;
import com.planitsquare.holiday.model.request.PageInfoRequest;
import com.planitsquare.holiday.store.ApiCallStore;
import com.planitsquare.holiday.store.HolidayManageStore;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HolidayManageServiceUnitTest {

    @Mock
    private ApiCallStore apiStore;

    @Mock
    private HolidayManageStore holidayManageStore;

    @InjectMocks
    private HolidayManageService holidayManageService;

    @Captor
    private ArgumentCaptor<List<CountryInfoDto>> countryListCaptor;

    @Captor
    private ArgumentCaptor<List<HolidayInfoDto>> holidayListCaptor;

    @Captor
    private ArgumentCaptor<List<HolidayCountryDto>> holidayCountryCaptor;

    @Captor
    private ArgumentCaptor<List<HolidayTypeDto>> holidayTypeCaptor;

    private List<CountryInfoDto> mockCountryList;
    private List<HolidayInfoDto> mockHolidayList;

    @BeforeEach
    void setUp() {
        // Mock 데이터 준비
        mockCountryList = createMockCountryList();
        mockHolidayList = createMockHolidayList();
    }

    @Nested
    @DisplayName("initHolidayData 메소드 테스트")
    class InitHolidayDataTest {

        @Test
        @Order(1)
        @DisplayName("초기 데이터가 없을 때 정상적으로 데이터 초기화")
        void testInitHolidayData_WithNoExistingData() {
            // Given
            List<HolidayInfoDto> savedList = createMockHolidayListWithSeq();

            when(apiStore.getCountryCodeList()).thenReturn(mockCountryList);
            when(apiStore.getHolidayInfoList(anyList(), anyList())).thenReturn(mockHolidayList);
            when(holidayManageStore.getHCountryInfoAll()).thenReturn(List.of());
            when(holidayManageStore.getHolidayInfoList(anyInt(), anyInt()))
                    .thenReturn(List.of())      // 첫 번째 호출 (삭제 체크용)
                    .thenReturn(savedList);     // 두 번째 호출 (추가 정보 삽입용)

            // When
            holidayManageService.initHolidayData();

            // Then
            verify(apiStore, times(1)).getCountryCodeList();
            verify(apiStore, times(1)).getHolidayInfoList(anyList(), anyList());
            verify(holidayManageStore, times(1)).countryInfoBatchInsert(mockCountryList);
            verify(holidayManageStore, times(1)).holidayInfoBatchInsert(mockHolidayList);
            verify(holidayManageStore, times(1)).bulkInsertHolidayAdditionalInfos(savedList);
            verify(holidayManageStore, never()).countryInfoBatchDelete();
            verify(holidayManageStore, never()).holidayInfoBatchDelete(anyList());
        }

        @Test
        @Order(2)
        @DisplayName("기존 데이터가 있을 때 삭제 후 재삽입")
        void testInitHolidayData_WithExistingData() {
            // Given
            List<CountryInfoDto> existingCountries = createMockCountryList();
            List<HolidayInfoDto> existingHolidays = createMockHolidayListWithSeq();
            List<HolidayInfoDto> savedList = createMockHolidayListWithCountiesAndTypes();

            when(apiStore.getCountryCodeList()).thenReturn(mockCountryList);
            when(apiStore.getHolidayInfoList(anyList(), anyList())).thenReturn(mockHolidayList);
            when(holidayManageStore.getHCountryInfoAll()).thenReturn(existingCountries);
            when(holidayManageStore.getHolidayInfoList(anyInt(), anyInt()))
                    .thenReturn(existingHolidays)   // 첫 번째 호출 (삭제용)
                    .thenReturn(savedList);         // 두 번째 호출 (추가 정보 삽입용)

            // When
            holidayManageService.initHolidayData();

            // Then
            verify(holidayManageStore, times(1)).countryInfoBatchDelete();
            verify(holidayManageStore, times(1)).holidayTypeBatchDelete(anyList());
            verify(holidayManageStore, times(1)).holidayCountryBatchDelete(anyList());
            verify(holidayManageStore, times(1)).holidayInfoBatchDelete(anyList());
            verify(holidayManageStore, times(1)).countryInfoBatchInsert(mockCountryList);
            verify(holidayManageStore, times(1)).holidayInfoBatchInsert(mockHolidayList);
            verify(holidayManageStore, times(1)).bulkInsertHolidayAdditionalInfos(savedList);

            // 삭제 시 올바른 ID 목록이 전달되었는지 검증
            ArgumentCaptor<List<Long>> deleteIdsCaptor = ArgumentCaptor.forClass(List.class);
            verify(holidayManageStore).holidayTypeBatchDelete(deleteIdsCaptor.capture());

            List<Long> capturedIds = deleteIdsCaptor.getValue();
            assertNotNull(capturedIds);
            assertFalse(capturedIds.isEmpty());
            assertEquals(existingHolidays.size(), capturedIds.size());
        }

        @Test
        @Order(3)
        @DisplayName("API 호출 실패 시 RestClientCallException 발생")
        void testInitHolidayData_ApiCallFailure() {
            // Given
            RestClientCallException exception = new RestClientCallException(
                    "API 호출 실패",
                    500,
                    "Internal Server Error"
            );
            when(apiStore.getCountryCodeList()).thenReturn(mockCountryList);
            when(apiStore.getHolidayInfoList(anyList(), anyList())).thenThrow(exception);

            // When & Then
            RestClientCallException thrown = assertThrows(
                    RestClientCallException.class,
                    () -> holidayManageService.initHolidayData()
            );

            assertEquals(exception, thrown);
            verify(holidayManageStore, never()).countryInfoBatchInsert(anyList());
            verify(holidayManageStore, never()).holidayInfoBatchInsert(anyList());
            verify(holidayManageStore, never()).bulkInsertHolidayAdditionalInfos(anyList());
        }

        @Test
        @Order(4)
        @DisplayName("예상치 못한 예외 발생 시 예외 전파")
        void testInitHolidayData_UnexpectedException() {
            // Given
            RuntimeException exception = new RuntimeException("Unexpected error");
            when(apiStore.getCountryCodeList()).thenReturn(mockCountryList);
            when(apiStore.getHolidayInfoList(anyList(), anyList())).thenThrow(exception);

            // When & Then
            assertThrows(RuntimeException.class, () -> holidayManageService.initHolidayData());
            verify(holidayManageStore, never()).countryInfoBatchInsert(anyList());
            verify(holidayManageStore, never()).bulkInsertHolidayAdditionalInfos(anyList());
        }

        @Test
        @Order(5)
        @DisplayName("추가 정보(Counties, Types) 일괄 삽입 검증")
        void testInitHolidayData_BulkInsertAdditionalInfos() {
            // Given
            List<HolidayInfoDto> savedList = createMockHolidayListWithCountiesAndTypes();

            when(apiStore.getCountryCodeList()).thenReturn(mockCountryList);
            when(apiStore.getHolidayInfoList(anyList(), anyList())).thenReturn(mockHolidayList);
            when(holidayManageStore.getHCountryInfoAll()).thenReturn(List.of());
            when(holidayManageStore.getHolidayInfoList(anyInt(), anyInt()))
                    .thenReturn(List.of())      // 첫 번째 호출
                    .thenReturn(savedList);     // 두 번째 호출

            // When
            holidayManageService.initHolidayData();

            // Then
            ArgumentCaptor<List<HolidayInfoDto>> listCaptor = ArgumentCaptor.forClass(List.class);
            verify(holidayManageStore, times(1)).bulkInsertHolidayAdditionalInfos(listCaptor.capture());

            List<HolidayInfoDto> capturedList = listCaptor.getValue();
            assertNotNull(capturedList);
            assertFalse(capturedList.isEmpty());
            assertEquals(savedList, capturedList);

            // Counties와 Types가 있는 데이터인지 확인
            assertTrue(capturedList.stream().anyMatch(h ->
                    h.getCounties() != null && !h.getCounties().isEmpty()));
            assertTrue(capturedList.stream().anyMatch(h ->
                    h.getTypes() != null && !h.getTypes().isEmpty()));
        }

        @Test
        @Order(6)
        @DisplayName("빈 추가 정보로도 bulkInsertHolidayAdditionalInfos 호출")
        void testInitHolidayData_BulkInsertWithEmptyAdditionalInfos() {
            // Given
            List<HolidayInfoDto> savedList = createMockHolidayListWithSeq();

            when(apiStore.getCountryCodeList()).thenReturn(mockCountryList);
            when(apiStore.getHolidayInfoList(anyList(), anyList())).thenReturn(mockHolidayList);
            when(holidayManageStore.getHCountryInfoAll()).thenReturn(List.of());
            when(holidayManageStore.getHolidayInfoList(anyInt(), anyInt()))
                    .thenReturn(List.of())
                    .thenReturn(savedList);

            // When
            holidayManageService.initHolidayData();

            // Then
            verify(holidayManageStore, times(1)).bulkInsertHolidayAdditionalInfos(savedList);
        }

        @Test
        @Order(7)
        @DisplayName("연도 범위 계산 검증 (현재부터 5년 전까지)")
        void testInitHolidayData_YearRangeCalculation() {
            // Given
            when(apiStore.getCountryCodeList()).thenReturn(mockCountryList);
            when(apiStore.getHolidayInfoList(anyList(), anyList())).thenReturn(mockHolidayList);
            when(holidayManageStore.getHCountryInfoAll()).thenReturn(List.of());
            when(holidayManageStore.getHolidayInfoList(anyInt(), anyInt()))
                    .thenReturn(List.of())
                    .thenReturn(createMockHolidayListWithSeq());

            // When
            holidayManageService.initHolidayData();

            // Then
            ArgumentCaptor<List<Integer>> yearCaptor = ArgumentCaptor.forClass(List.class);
            verify(apiStore).getHolidayInfoList(yearCaptor.capture(), anyList());

            List<Integer> capturedYears = yearCaptor.getValue();
            assertEquals(6, capturedYears.size()); // 0~5년 = 6개

            int currentYear = LocalDate.now().getYear();
            assertTrue(capturedYears.contains(currentYear));
            assertTrue(capturedYears.contains(currentYear - 5));
        }

        @Test
        @Order(8)
        @DisplayName("getHolidayInfoList가 yearList의 getLast()와 getFirst()로 호출되는지 검증")
        void testInitHolidayData_VerifyGetHolidayInfoListParameters() {
            // Given
            List<HolidayInfoDto> savedList = createMockHolidayListWithSeq();

            when(apiStore.getCountryCodeList()).thenReturn(mockCountryList);
            when(apiStore.getHolidayInfoList(anyList(), anyList())).thenReturn(mockHolidayList);
            when(holidayManageStore.getHCountryInfoAll()).thenReturn(List.of());
            when(holidayManageStore.getHolidayInfoList(anyInt(), anyInt()))
                    .thenReturn(List.of())
                    .thenReturn(savedList);

            // When
            holidayManageService.initHolidayData();

            // Then
            ArgumentCaptor<Integer> firstYearCaptor = ArgumentCaptor.forClass(Integer.class);
            ArgumentCaptor<Integer> lastYearCaptor = ArgumentCaptor.forClass(Integer.class);

            verify(holidayManageStore, times(2))
                    .getHolidayInfoList(lastYearCaptor.capture(), firstYearCaptor.capture());

            // yearList는 현재부터 5년 전까지이므로
            int currentYear = LocalDate.now().getYear();
            int expectedFirst = currentYear;
            int expectedLast = currentYear - 5;

            // 두 번째 호출 (인덱스 1)의 파라미터 검증
            assertEquals(expectedLast, lastYearCaptor.getAllValues().get(1));
            assertEquals(expectedFirst, firstYearCaptor.getAllValues().get(1));
        }

        @Test
        @Order(9)
        @DisplayName("DB 삽입 중 예외 발생 시 트랜잭션 롤백 확인")
        void testInitHolidayData_TransactionRollback() {
            // Given
            RuntimeException dbException = new RuntimeException("Database error");

            when(apiStore.getCountryCodeList()).thenReturn(mockCountryList);
            when(apiStore.getHolidayInfoList(anyList(), anyList())).thenReturn(mockHolidayList);
            when(holidayManageStore.getHCountryInfoAll()).thenReturn(List.of());
            when(holidayManageStore.getHolidayInfoList(anyInt(), anyInt())).thenReturn(List.of());
            doThrow(dbException).when(holidayManageStore).countryInfoBatchInsert(anyList());

            // When & Then
            assertThrows(RuntimeException.class, () -> holidayManageService.initHolidayData());

            // countryInfoBatchInsert에서 예외 발생했으므로 이후 메소드는 호출되지 않아야 함
            verify(holidayManageStore, times(1)).countryInfoBatchInsert(anyList());
            verify(holidayManageStore, never()).holidayInfoBatchInsert(anyList());
            verify(holidayManageStore, never()).bulkInsertHolidayAdditionalInfos(anyList());
        }
    }

    @Nested
    @DisplayName("getHolidayInfoByCountry 메소드 테스트")
    class GetHolidayInfoByCountryTest {

        @Test
        @Order(8)
        @DisplayName("유효한 국가 코드로 휴일 정보 조회 성공")
        void testGetHolidayInfoByCountry_Success() {
            // Given
            String country = "KR";
            PageInfoRequest pageInfo = PageInfoRequest.builder()
                    .page(1)
                    .size(10)
                    .sortTarget("date")
                    .sort("ASC")
                    .build();
            SearchFilterDto filter = new SearchFilterDto();

            Page<HolidayInfoDto> expectedPage = new PageImpl<>(
                    mockHolidayList,
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "date")),
                    mockHolidayList.size()
            );

            when(holidayManageStore.existsByCountryCode(country)).thenReturn(true);
            when(holidayManageStore.getHolidayInfoByCountry(eq(country), any(PageRequest.class), eq(filter)))
                    .thenReturn(expectedPage);

            // When
            Page<HolidayInfoDto> result = holidayManageService.getHolidayInfoByCountry(country, pageInfo, filter);

            // Then
            assertNotNull(result);
            assertEquals(mockHolidayList.size(), result.getContent().size());
            verify(holidayManageStore, times(1)).existsByCountryCode(country);
            verify(holidayManageStore, times(1)).getHolidayInfoByCountry(eq(country), any(PageRequest.class), eq(filter));
        }

        @Test
        @Order(9)
        @DisplayName("존재하지 않는 국가 코드로 조회 시 DateNotExistException 발생")
        void testGetHolidayInfoByCountry_CountryNotExist() {
            // Given
            String country = "INVALID";
            PageInfoRequest pageInfo = PageInfoRequest.builder()
                    .page(1)
                    .size(10)
                    .build();
            SearchFilterDto filter = new SearchFilterDto();

            when(holidayManageStore.existsByCountryCode(country)).thenReturn(true);

            // When & Then
            DateNotExistException exception = assertThrows(
                    DateNotExistException.class,
                    () -> holidayManageService.getHolidayInfoByCountry(country, pageInfo, filter)
            );

            assertTrue(exception.getMessage().contains(country));
            verify(holidayManageStore, times(1)).existsByCountryCode(country);
            verify(holidayManageStore, never()).getHolidayInfoByCountry(anyString(), any(PageRequest.class), any());
        }

        @Test
        @Order(10)
        @DisplayName("정렬 옵션이 없을 때 기본 정렬(날짜 오름차순) 적용")
        void testGetHolidayInfoByCountry_DefaultSort() {
            // Given
            String country = "KR";
            PageInfoRequest pageInfo = PageInfoRequest.builder()
                    .page(1)
                    .size(10)
                    .build();
            SearchFilterDto filter = new SearchFilterDto();

            Page<HolidayInfoDto> expectedPage = new PageImpl<>(mockHolidayList);

            when(holidayManageStore.existsByCountryCode(country)).thenReturn(true);
            when(holidayManageStore.getHolidayInfoByCountry(eq(country), any(PageRequest.class), eq(filter)))
                    .thenReturn(expectedPage);

            // When
            holidayManageService.getHolidayInfoByCountry(country, pageInfo, filter);

            // Then
            ArgumentCaptor<PageRequest> pageableCaptor = ArgumentCaptor.forClass(PageRequest.class);
            verify(holidayManageStore).getHolidayInfoByCountry(eq(country), pageableCaptor.capture(), eq(filter));

            Pageable capturedPageable = pageableCaptor.getValue();
            assertEquals(Sort.by(Sort.Direction.ASC, HolidaySortType.DATE.name()), capturedPageable.getSort());
        }

        @Test
        @Order(11)
        @DisplayName("DESC 정렬로 조회")
        void testGetHolidayInfoByCountry_DescSort() {
            // Given
            String country = "US";
            PageInfoRequest pageInfo = PageInfoRequest.builder()
                    .page(1)
                    .size(10)
                    .sortTarget("date")
                    .sort("DESC")
                    .build();
            SearchFilterDto filter = new SearchFilterDto();

            Page<HolidayInfoDto> expectedPage = new PageImpl<>(mockHolidayList);

            when(holidayManageStore.existsByCountryCode(country)).thenReturn(true);
            when(holidayManageStore.getHolidayInfoByCountry(eq(country), any(PageRequest.class), eq(filter)))
                    .thenReturn(expectedPage);

            // When
            holidayManageService.getHolidayInfoByCountry(country, pageInfo, filter);

            // Then
            ArgumentCaptor<PageRequest> pageableCaptor = ArgumentCaptor.forClass(PageRequest.class);
            verify(holidayManageStore).getHolidayInfoByCountry(eq(country), pageableCaptor.capture(), eq(filter));

            Pageable capturedPageable = pageableCaptor.getValue();
            assertEquals(Sort.by(Sort.Direction.DESC, "date"), capturedPageable.getSort());
        }

        @Test
        @Order(12)
        @DisplayName("페이지 번호가 0부터 시작하도록 변환")
        void testGetHolidayInfoByCountry_PageNumberConversion() {
            // Given
            String country = "KR";
            PageInfoRequest pageInfo = PageInfoRequest.builder()
                    .page(3)  // 3페이지 요청
                    .size(20)
                    .build();
            SearchFilterDto filter = new SearchFilterDto();

            Page<HolidayInfoDto> expectedPage = new PageImpl<>(mockHolidayList);

            when(holidayManageStore.existsByCountryCode(country)).thenReturn(true);
            when(holidayManageStore.getHolidayInfoByCountry(eq(country), any(PageRequest.class), eq(filter)))
                    .thenReturn(expectedPage);

            // When
            holidayManageService.getHolidayInfoByCountry(country, pageInfo, filter);

            // Then
            ArgumentCaptor<PageRequest> pageableCaptor = ArgumentCaptor.forClass(PageRequest.class);
            verify(holidayManageStore).getHolidayInfoByCountry(eq(country), pageableCaptor.capture(), eq(filter));

            Pageable capturedPageable = pageableCaptor.getValue();
            assertEquals(2, capturedPageable.getPageNumber()); // 3-1 = 2
            assertEquals(20, capturedPageable.getPageSize());
        }
    }

    @Nested
    @DisplayName("getHolidayInfoByYear 메소드 테스트")
    class GetHolidayInfoByYearTest {

        @Test
        @Order(13)
        @DisplayName("특정 연도의 휴일 정보 조회 성공")
        void testGetHolidayInfoByYear_Success() {
            // Given
            int year = 2024;
            PageInfoRequest pageInfo = PageInfoRequest.builder()
                    .page(1)
                    .size(10)
                    .sortTarget("date")
                    .sort("ASC")
                    .build();
            SearchFilterDto filter = new SearchFilterDto();

            Page<HolidayInfoDto> expectedPage = new PageImpl<>(
                    mockHolidayList,
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "date")),
                    mockHolidayList.size()
            );

            when(holidayManageStore.getHolidayInfoByYear(eq(year), any(PageRequest.class), eq(filter)))
                    .thenReturn(expectedPage);

            // When
            Page<HolidayInfoDto> result = holidayManageService.getHolidayInfoByYear(year, pageInfo, filter);

            // Then
            assertNotNull(result);
            assertEquals(mockHolidayList.size(), result.getContent().size());
            verify(holidayManageStore, times(1)).getHolidayInfoByYear(eq(year), any(PageRequest.class), eq(filter));
        }

        @Test
        @Order(14)
        @DisplayName("정렬 옵션 없이 조회 시 기본 정렬 적용")
        void testGetHolidayInfoByYear_DefaultSort() {
            // Given
            int year = 2024;
            PageInfoRequest pageInfo = PageInfoRequest.builder()
                    .page(1)
                    .size(10)
                    .build();
            SearchFilterDto filter = new SearchFilterDto();

            Page<HolidayInfoDto> expectedPage = new PageImpl<>(mockHolidayList);

            when(holidayManageStore.getHolidayInfoByYear(eq(year), any(PageRequest.class), eq(filter)))
                    .thenReturn(expectedPage);

            // When
            holidayManageService.getHolidayInfoByYear(year, pageInfo, filter);

            // Then
            ArgumentCaptor<PageRequest> pageableCaptor = ArgumentCaptor.forClass(PageRequest.class);
            verify(holidayManageStore).getHolidayInfoByYear(eq(year), pageableCaptor.capture(), eq(filter));

            Pageable capturedPageable = pageableCaptor.getValue();
            assertEquals(Sort.by(Sort.Direction.ASC, HolidaySortType.DATE.name()), capturedPageable.getSort());
        }

        @Test
        @Order(15)
        @DisplayName("다양한 페이징 옵션으로 조회")
        void testGetHolidayInfoByYear_DifferentPagingOptions() {
            // Given
            int year = 2023;
            PageInfoRequest pageInfo = PageInfoRequest.builder()
                    .page(5)
                    .size(50)
                    .sortTarget("name")
                    .sort("DESC")
                    .build();
            SearchFilterDto filter = new SearchFilterDto();

            Page<HolidayInfoDto> expectedPage = new PageImpl<>(mockHolidayList);

            when(holidayManageStore.getHolidayInfoByYear(eq(year), any(PageRequest.class), eq(filter)))
                    .thenReturn(expectedPage);

            // When
            holidayManageService.getHolidayInfoByYear(year, pageInfo, filter);

            // Then
            ArgumentCaptor<PageRequest> pageableCaptor = ArgumentCaptor.forClass(PageRequest.class);
            verify(holidayManageStore).getHolidayInfoByYear(eq(year), pageableCaptor.capture(), eq(filter));

            Pageable capturedPageable = pageableCaptor.getValue();
            assertEquals(4, capturedPageable.getPageNumber()); // 5-1 = 4
            assertEquals(50, capturedPageable.getPageSize());
            assertEquals(Sort.by(Sort.Direction.DESC, "name"), capturedPageable.getSort());
        }

        @Test
        @Order(16)
        @DisplayName("잘못된 정렬 대상이 주어진 경우 기본 정렬 적용")
        void testGetHolidayInfoByYear_InvalidSortTarget() {
            // Given
            int year = 2024;
            PageInfoRequest pageInfo = PageInfoRequest.builder()
                    .page(1)
                    .size(10)
                    .sortTarget("invalid_field")
                    .sort("ASC")
                    .build();
            SearchFilterDto filter = new SearchFilterDto();

            Page<HolidayInfoDto> expectedPage = new PageImpl<>(mockHolidayList);

            when(holidayManageStore.getHolidayInfoByYear(eq(year), any(PageRequest.class), eq(filter)))
                    .thenReturn(expectedPage);

            // When
            holidayManageService.getHolidayInfoByYear(year, pageInfo, filter);

            // Then
            ArgumentCaptor<PageRequest> pageableCaptor = ArgumentCaptor.forClass(PageRequest.class);
            verify(holidayManageStore).getHolidayInfoByYear(eq(year), pageableCaptor.capture(), eq(filter));

            // HolidaySortType.valueOfString()이 null을 반환하면 기본 정렬 적용
            Pageable capturedPageable = pageableCaptor.getValue();
            assertEquals(Sort.by(Sort.Direction.ASC, HolidaySortType.DATE.name()), capturedPageable.getSort());
        }
    }

    @Nested
    @DisplayName("refreshHolidayData 메소드 테스트")
    class RefreshHolidayDataTest {

        @Test
        @Order(17)
        @DisplayName("데이터가 존재하지 않을 때 새로 삽입")
        void testRefreshHolidayData_InsertNew() {
            // Given
            int year = 2024;
            String countryCode = "KR";
            List<HolidayInfoDto> holidayInfoList = createMockHolidayList();
            List<HolidayInfoDto> savedList = createMockHolidayListWithSeq();

            when(apiStore.getHolidayList(year, countryCode)).thenReturn(holidayInfoList);
            when(holidayManageStore.existsHolidayInfoByYearAndCountryCode(year, countryCode))
                    .thenReturn(false);
            when(holidayManageStore.getHolidayInfoList(year, countryCode))
                    .thenReturn(savedList);

            // When
            holidayManageService.refreshHolidayData(year, countryCode);

            // Then
            verify(apiStore, times(1)).getHolidayList(year, countryCode);
            verify(holidayManageStore, times(1)).existsHolidayInfoByYearAndCountryCode(year, countryCode);
            verify(holidayManageStore, times(1)).holidayInfoBatchInsert(holidayInfoList);
            verify(holidayManageStore, times(1)).getHolidayInfoList(year, countryCode);
            verify(holidayManageStore, times(1)).bulkInsertHolidayAdditionalInfos(savedList);
            verify(holidayManageStore, never()).refreshHolidayInfo(anyInt(), anyString(), anyList());
        }

        @Test
        @Order(18)
        @DisplayName("데이터가 이미 존재할 때 갱신")
        void testRefreshHolidayData_UpdateExisting() {
            // Given
            int year = 2024;
            String countryCode = "US";
            List<HolidayInfoDto> holidayInfoList = createMockHolidayList();

            when(apiStore.getHolidayList(year, countryCode)).thenReturn(holidayInfoList);
            when(holidayManageStore.existsHolidayInfoByYearAndCountryCode(year, countryCode))
                    .thenReturn(true);

            // When
            holidayManageService.refreshHolidayData(year, countryCode);

            // Then
            verify(apiStore, times(1)).getHolidayList(year, countryCode);
            verify(holidayManageStore, times(1)).existsHolidayInfoByYearAndCountryCode(year, countryCode);
            verify(holidayManageStore, times(1)).refreshHolidayInfo(year, countryCode, holidayInfoList);
            verify(holidayManageStore, never()).holidayInfoBatchInsert(anyList());
            verify(holidayManageStore, never()).getHolidayInfoList(anyInt(), anyString());
            verify(holidayManageStore, never()).bulkInsertHolidayAdditionalInfos(anyList());
        }

        @Test
        @Order(19)
        @DisplayName("API 호출 실패 시 RestClientCallException 발생")
        void testRefreshHolidayData_ApiCallFailure() {
            // Given
            int year = 2024;
            String countryCode = "KR";
            RestClientCallException exception = new RestClientCallException(
                    "API 호출 실패",
                    500,
                    "Internal Server Error"
            );

            when(apiStore.getHolidayList(year, countryCode)).thenThrow(exception);

            // When & Then
            RestClientCallException thrown = assertThrows(
                    RestClientCallException.class,
                    () -> holidayManageService.refreshHolidayData(year, countryCode)
            );

            assertEquals(exception, thrown);
            verify(apiStore, times(1)).getHolidayList(year, countryCode);
            verify(holidayManageStore, never()).existsHolidayInfoByYearAndCountryCode(anyInt(), anyString());
            verify(holidayManageStore, never()).holidayInfoBatchInsert(anyList());
            verify(holidayManageStore, never()).refreshHolidayInfo(anyInt(), anyString(), anyList());
        }

        @Test
        @Order(20)
        @DisplayName("새 데이터 삽입 시 추가 정보도 함께 삽입")
        void testRefreshHolidayData_InsertWithAdditionalInfo() {
            // Given
            int year = 2023;
            String countryCode = "JP";
            List<HolidayInfoDto> holidayInfoList = createMockHolidayList();
            List<HolidayInfoDto> savedList = createMockHolidayListWithCountiesAndTypes();

            when(apiStore.getHolidayList(year, countryCode)).thenReturn(holidayInfoList);
            when(holidayManageStore.existsHolidayInfoByYearAndCountryCode(year, countryCode))
                    .thenReturn(false);
            when(holidayManageStore.getHolidayInfoList(year, countryCode))
                    .thenReturn(savedList);

            // When
            holidayManageService.refreshHolidayData(year, countryCode);

            // Then
            verify(holidayManageStore, times(1)).holidayInfoBatchInsert(holidayInfoList);
            verify(holidayManageStore, times(1)).bulkInsertHolidayAdditionalInfos(savedList);

            // bulkInsertHolidayAdditionalInfos에 전달된 리스트 검증
            ArgumentCaptor<List<HolidayInfoDto>> listCaptor = ArgumentCaptor.forClass(List.class);
            verify(holidayManageStore).bulkInsertHolidayAdditionalInfos(listCaptor.capture());

            List<HolidayInfoDto> capturedList = listCaptor.getValue();
            assertNotNull(capturedList);
            assertFalse(capturedList.isEmpty());
            assertTrue(capturedList.stream().allMatch(h -> h.getHolidayInfoSeq() != null));
        }

        @Test
        @Order(21)
        @DisplayName("빈 휴일 목록으로 호출 시에도 정상 처리")
        void testRefreshHolidayData_EmptyHolidayList() {
            // Given
            int year = 2024;
            String countryCode = "XX";
            List<HolidayInfoDto> emptyList = List.of();

            when(apiStore.getHolidayList(year, countryCode)).thenReturn(emptyList);
            when(holidayManageStore.existsHolidayInfoByYearAndCountryCode(year, countryCode))
                    .thenReturn(false);
            when(holidayManageStore.getHolidayInfoList(year, countryCode))
                    .thenReturn(emptyList);

            // When
            holidayManageService.refreshHolidayData(year, countryCode);

            // Then
            verify(holidayManageStore, times(1)).holidayInfoBatchInsert(emptyList);
            verify(holidayManageStore, times(1)).bulkInsertHolidayAdditionalInfos(emptyList);
        }

        @Test
        @Order(22)
        @DisplayName("갱신 시 올바른 파라미터로 호출되는지 검증")
        void testRefreshHolidayData_VerifyRefreshParameters() {
            // Given
            int year = 2025;
            String countryCode = "CN";
            List<HolidayInfoDto> holidayInfoList = createMockHolidayList();

            when(apiStore.getHolidayList(year, countryCode)).thenReturn(holidayInfoList);
            when(holidayManageStore.existsHolidayInfoByYearAndCountryCode(year, countryCode))
                    .thenReturn(true);

            // When
            holidayManageService.refreshHolidayData(year, countryCode);

            // Then
            ArgumentCaptor<Integer> yearCaptor = ArgumentCaptor.forClass(Integer.class);
            ArgumentCaptor<String> countryCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<List<HolidayInfoDto>> listCaptor = ArgumentCaptor.forClass(List.class);

            verify(holidayManageStore).refreshHolidayInfo(
                    yearCaptor.capture(),
                    countryCaptor.capture(),
                    listCaptor.capture()
            );

            assertEquals(year, yearCaptor.getValue());
            assertEquals(countryCode, countryCaptor.getValue());
            assertEquals(holidayInfoList, listCaptor.getValue());
        }

        @Test
        @Order(23)
        @DisplayName("DB 삽입 실패 시 예외 전파")
        void testRefreshHolidayData_DatabaseInsertFailure() {
            // Given
            int year = 2024;
            String countryCode = "KR";
            List<HolidayInfoDto> holidayInfoList = createMockHolidayList();
            RuntimeException dbException = new RuntimeException("Database insert failed");

            when(apiStore.getHolidayList(year, countryCode)).thenReturn(holidayInfoList);
            when(holidayManageStore.existsHolidayInfoByYearAndCountryCode(year, countryCode))
                    .thenReturn(false);
            doThrow(dbException).when(holidayManageStore).holidayInfoBatchInsert(anyList());

            // When & Then
            RuntimeException thrown = assertThrows(
                    RuntimeException.class,
                    () -> holidayManageService.refreshHolidayData(year, countryCode)
            );

            assertEquals(dbException, thrown);
            verify(holidayManageStore, times(1)).holidayInfoBatchInsert(holidayInfoList);
            verify(holidayManageStore, never()).bulkInsertHolidayAdditionalInfos(anyList());
        }

        @Test
        @Order(24)
        @DisplayName("갱신 실패 시 예외 전파")
        void testRefreshHolidayData_RefreshFailure() {
            // Given
            int year = 2024;
            String countryCode = "US";
            List<HolidayInfoDto> holidayInfoList = createMockHolidayList();
            RuntimeException refreshException = new RuntimeException("Refresh failed");

            when(apiStore.getHolidayList(year, countryCode)).thenReturn(holidayInfoList);
            when(holidayManageStore.existsHolidayInfoByYearAndCountryCode(year, countryCode))
                    .thenReturn(true);
            doThrow(refreshException).when(holidayManageStore)
                    .refreshHolidayInfo(anyInt(), anyString(), anyList());

            // When & Then
            RuntimeException thrown = assertThrows(
                    RuntimeException.class,
                    () -> holidayManageService.refreshHolidayData(year, countryCode)
            );

            assertEquals(refreshException, thrown);
            verify(holidayManageStore, times(1))
                    .refreshHolidayInfo(year, countryCode, holidayInfoList);
        }

        @Test
        @Order(25)
        @DisplayName("여러 연도와 국가에 대해 순차적으로 호출")
        void testRefreshHolidayData_MultipleCallsSequentially() {
            // Given
            List<Integer> years = List.of(2023, 2024);
            List<String> countries = List.of("KR", "US");
            List<HolidayInfoDto> holidayInfoList = createMockHolidayList();

            when(apiStore.getHolidayList(anyInt(), anyString())).thenReturn(holidayInfoList);
            when(holidayManageStore.existsHolidayInfoByYearAndCountryCode(anyInt(), anyString()))
                    .thenReturn(false);
            when(holidayManageStore.getHolidayInfoList(anyInt(), anyString()))
                    .thenReturn(createMockHolidayListWithSeq());

            // When
            for (Integer year : years) {
                for (String country : countries) {
                    holidayManageService.refreshHolidayData(year, country);
                }
            }

            // Then - 총 4번(2년 × 2국가) 호출되어야 함
            verify(apiStore, times(4)).getHolidayList(anyInt(), anyString());
            verify(holidayManageStore, times(4))
                    .existsHolidayInfoByYearAndCountryCode(anyInt(), anyString());
            verify(holidayManageStore, times(4)).holidayInfoBatchInsert(anyList());
            verify(holidayManageStore, times(4)).bulkInsertHolidayAdditionalInfos(anyList());
        }
    }

    // Helper methods for creating mock data
    private List<CountryInfoDto> createMockCountryList() {
        List<CountryInfoDto> list = new ArrayList<>();

        CountryInfoDto kr = new CountryInfoDto();
        kr.setCountryCode("KR");
        kr.setName("South Korea");

        CountryInfoDto us = new CountryInfoDto();
        us.setCountryCode("US");
        us.setName("United States");

        list.add(kr);
        list.add(us);

        return list;
    }

    private List<HolidayInfoDto> createMockHolidayList() {
        List<HolidayInfoDto> list = new ArrayList<>();

        HolidayInfoDto holiday1 = new HolidayInfoDto();
        holiday1.setDate(LocalDate.of(2024, 1, 1));
        holiday1.setName("New Year's Day");
        holiday1.setLocalName("신정");
        holiday1.setCountryCode("KR");
        holiday1.setHolidayYear(2024);

        HolidayInfoDto holiday2 = new HolidayInfoDto();
        holiday2.setDate(LocalDate.of(2024, 3, 1));
        holiday2.setName("Independence Movement Day");
        holiday2.setLocalName("삼일절");
        holiday2.setCountryCode("KR");
        holiday2.setHolidayYear(2024);

        list.add(holiday1);
        list.add(holiday2);

        return list;
    }

    private List<HolidayInfoDto> createMockHolidayListWithSeq() {
        List<HolidayInfoDto> list = createMockHolidayList();
        list.get(0).setHolidayInfoSeq(1L);
        list.get(1).setHolidayInfoSeq(2L);
        return list;
    }

    private List<HolidayInfoDto> createMockHolidayListWithCountiesAndTypes() {
        List<HolidayInfoDto> list = createMockHolidayListWithSeq();

        list.get(0).setCounties(List.of("Seoul", "Busan"));
        list.get(0).setTypes(List.of("Public", "National"));

        list.get(1).setCounties(List.of("Seoul"));
        list.get(1).setTypes(List.of("Public"));

        return list;
    }
}