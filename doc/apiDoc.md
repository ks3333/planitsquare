# 글로벌 휴일 관리 서비스

**Version:** v1

세계 각국의 휴일을 검색 및 관리 할 수 있는 서비스 입니다

---

## 📌 API Endpoints

### PUT `/api/holiday/refresh/{country}/{year}`

**공휴일 데이터 갱신**

호출한 국가/연도의 공휴일 데이터를 갱신합니다


#### 🔸 Parameters

| Name | In | Required | Type | Description |
|------|-----|----------|-------|-------------|
| country | path | True | string | 국가코드 |
| year | path | True | integer | 연도 |

#### 🔸 Responses

- **200** : 성공
- **503** : 외부 API 오류 혹은 기타 원인으로 인한 실패

#### 🔸 Responses Example

#### Request
```
PUT /api/holiday/refresh/US/2023
```

#### Response
```json
{
  "result": "SUCCESS"
}
```

---

### GET `/api/holiday/search/year/{year}`

**연도별 공휴일 데이터 검색**

연도별 공휴일 데이터를 검색합니다.


#### 🔸 Parameters

| Name | In | Required | Type | Description |
|------|-----|----------|-------|-------------|
| year | path | True | integer | 연도 |
| fromDate | query | False | string | 시작일 |
| toDate | query | False | string | 종료일 |
| counties | query | False | array | 시/도 코드 목록 : EX - US-CA, US-NY |
| types | query | False | array | 공휴일 종류 목록 : EX - Public, Bank, School, Authorities |
| countryCode | query | False | string | 국가 코드 |
| page | query | False | integer | 페이지 번호 |
| size | query | False | integer | 페이지 사이즈 |
| sortTarget | query | False | string | 정렬 대상 |

#### 🔸 Responses

- **200** : 성공
- **503** : 외부 API 오류 혹은 기타 원인으로 인한 실패

#### 🔸 Responses Example

#### Request
```
GET /api/holiday/search/year/2023?countryCode=US&page=0&size=10&sortTarget=date
```

#### Response
```json
{
  "content": [
    {
      "holidayInfoSeq": 1,
      "holidayYear": 2023,
      "date": "2023-01-01",
      "localName": "New Year's Day",
      "name": "New Year's Day",
      "countryCode": "US",
      "fixed": true,
      "global": true,
      "counties": null,
      "launchYear": 1885,
      "types": [
        "Public"
      ]
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "pageNumber": 0,
    "pageSize": 10,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 1,
  "totalElements": 1,
  "last": true,
  "size": 10,
  "number": 0,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "first": true,
  "numberOfElements": 1,
  "empty": false
}
```
---

### GET `/api/holiday/search/country/{countryCode}`

**국가별 공휴일 데이터 검색**

국가별 공휴일 데이터 검색합니다.


#### 🔸 Parameters

| Name | In | Required | Type | Description |
|------|-----|----------|-------|-------------|
| countryCode | path | True | string | 국가코드 |
| fromDate | query | False | string | 시작일 |
| toDate | query | False | string | 종료일 |
| counties | query | False | array | 시/도 코드 목록 : EX - US-CA, US-NY |
| types | query | False | array | 공휴일 종류 목록 : EX - Public, Bank, School, Authorities |
| year | query | False | integer | 연도 |
| page | query | False | integer | 페이지 번호 |
| size | query | False | integer | 페이지 사이즈 |
| sortTarget | query | False | string | 정렬 대상 |

#### 🔸 Responses

- **200** : 성공
- **503** : 외부 API 오류 혹은 기타 원인으로 인한 실패
- **404** : 해당 국가의 공휴일 데이터가 존재하지 않습니다.

#### 🔸 Responses Example

#### Request
```
GET /api/holiday/search/country/US?year=2023&page=0&size=10&sortTarget=date
```
#### Response
```json
{
  "content": [
    {
      "holidayInfoSeq": 1,
      "holidayYear": 2023,
      "date": "2023-01-01",
      "localName": "New Year's Day",
      "name": "New Year's Day",
      "countryCode": "US",
      "fixed": true,
      "global": true,
      "counties": null,
      "launchYear": 1885,
      "types": [
        "Public"
      ]
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "pageNumber": 0,
    "pageSize": 10,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 1,
  "totalElements": 1,
  "last": true,
  "size": 10,
  "number": 0,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "first": true,
  "numberOfElements": 1,
  "empty": false
}
```
---

### PUT `/api/holiday/data/init`

**공휴일 데이터 초기화**

공휴일 데이터를 초기화합니다. 실행일의 년도와 해당 년도를 제외한 5년전 데이터를 생성합니다.


#### 🔸 Responses

- **200** : 성공
- **503** : 외부 API 오류 혹은 기타 원인으로 인한 실패

#### 🔸 Responses Example

#### Request
```
PUT /api/holiday/data/init
```

#### Response
```json
{
  "result": "SUCCESS"
}
```

---

### DELETE `/api/holiday/{country}/{year}`

**공휴일 데이터 삭제**

호출한 국가/연도의 공휴일 데이터를 삭제합니다


#### 🔸 Parameters

| Name | In | Required | Type | Description |
|------|-----|----------|-------|-------------|
| country | path | True | string | 국가코드 |
| year | path | True | integer | 연도 |

#### 🔸 Responses

- **200** : 성공
- **503** : 외부 API 오류 혹은 기타 원인으로 인한 실패

#### 🔸 Responses Example

#### Request
```
DELETE /api/holiday/US/2023
```
#### Response
```json
{
  "result": "SUCCESS"
}
```

---

## 📦 Schemas

### 🧩 CommonResponseString

| Field | Type | Description |
|-------|-------|-------------|
| result | string | 결과 내용   |


### 🧩 HolidayInfoDto

| Field | Type | Description |
|-------|-------|-------------|
| holidayInfoSeq | integer | 공휴일 정보 key  |
| holidayYear | integer | 공휴일 연도      |
| date | string | 공휴일 일자      |
| localName | string | 공휴일 지역 명칭   |
| name | string | 공휴일 명칭      |
| countryCode | string | 국가 코드       |
| fixed | boolean | 고정 휴일 여부    |
| global | boolean | 국제 공휴일 여부   |
| counties | array | 해당 지역       |
| launchYear | integer | 지정일         |
| types | array | 공휴일 종류      |
