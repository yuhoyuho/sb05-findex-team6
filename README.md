# **Findex**

<img width="1920" height="1080" alt="슬라이드4" src="https://github.com/user-attachments/assets/ac87a441-6908-46e1-a915-0548e5ff0140" />



## **팀원 구성**

강동민 (https://github.com/DONGMIN-777)

김용희 (https://github.com/backKim1024)

이유호 (https://github.com/yuhoyuho)

정수진 (https://github.com/5ranke)

정영진 (https://github.com/userjin2123)



## **프로젝트 소개**

- 금융 지수 데이터를 한눈에 제공하는 대시보드 서비스
- 프로젝트 기간: 2025.09.05 ~ 2025.09.15



## **기술 스택**

- Backend: Spring Boot, Spring Data JPA
- Database: PostgreSQL
- 공통 Tool: Git & Github, Discord, Notion
- 배포 : Railway.io



## **팀원별 구현 기능 상세**

### 강동민
- 대시 보드 관리
    - 주요 지수 : 일/월/연간 조회
    - 지수 차트 : 종가, 5일, 10일 이동 평균선 차트 조회
    - 지수 성과 : 지수/기관 별 지수 성과표 조회
  
### 김용희
- 지수 정보 관리
    - 지수 : 등록, 수정, 삭제 조건 조회
    - Open API : 기간별 데이터 연동

### 이유호
- 연동 작업 관리
    - 연동 개요 : 최근 7일간 성공, 실패 건 출력 / 마지막 연동 시간 출력
    - 연동 이력 : 조건별 이력 조회

### 정수진
- 자동 연동 설정 관리
    - 자동 연동 지수 조회 : 지수명 / 자동 연동 활성화 여부 별 조회
    - 자동 연동 지수 활성화 설정

### 정영진
- 지수 데이터 관리
    - 지수 데이터 : 등록, 수정, 삭제 조건 조회
    - 엑셀 : 목록 내보내기
    - Open API : 기간별 데이터 연동



## **파일 구조**

```
src
 ┣ main
 ┃ ┣ java
 ┃ ┃ ┗ com
 ┃ ┃ ┃ ┗ example
 ┃ ┃ ┃ ┃ ┗ findex
 ┃ ┃ ┃ ┃ ┃ ┣ FindexApplication.java
 ┃ ┃ ┃ ┃ ┃ ┣ Test.java
 ┃ ┃ ┃ ┃ ┃ ┣ common
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ base
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ BaseEntity.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ JobResult.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ JobType.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ SourceType.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ openApi
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ IndexApiResponseDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ service
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ IndexSyncService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ OpenApiService.java
 ┃ ┃ ┃ ┃ ┃ ┣ config
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ AppConfig.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ QueryDslConfig.java
 ┃ ┃ ┃ ┃ ┃ ┗ domain
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ Auto_Sync
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ AutoSyncController.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ AutoSyncConfigDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ AutoSyncConfigUpdateRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ CursorPageResponseAutoSyncConfigDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ AutoSync.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ mapper
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ AutoSyncMapper.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ repository
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ AutoSyncRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ service
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ AutoSyncService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ Index_data
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ IndexDataController.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ ChartDataPoint.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ CursorPageResponseIndexDataDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ IndexChartResponse.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ IndexDataCreateRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ IndexDataDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ IndexDataUpdateRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ IndexPerformanceDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ PeriodType.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ RankedIndexPerformanceDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ IndexData.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ mapper
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ IndexDataMapper.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ repository
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ IndexDataRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ IndexDataRepositoryCustom.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ impl
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ IndexDataRepositoryImpl.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ service
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ IndexDataService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ Index_Info
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ IndexInfoController.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ CursorPageResponseIndexInfoDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ IndexInfoCreateRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ IndexInfoDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ IndexInfoSummaryDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ IndexInfoUpdateDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ IndexInfo.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ mapper
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ IndexInfoMapper.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ repository
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ IndexInfoRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ IndexInfoRepositoryCustom.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ Impl
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ IndexInfoRepositoryImpl.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ service
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ IndexInfoService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ Sync_Job_Log
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ SyncJobLogController.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ CursorPageResponse.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ IndexDataSyncRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ SyncJobLogDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ SyncJobQueryParams.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ SyncJobSummaryDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ SyncResult.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ SyncJobLog.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ mapper
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ SyncJobLogMapper.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ repository
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ SyncJobLogRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ SyncJobLogRepositoryCustom.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ service
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ IndexDataScheduler.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ SyncJobLogService.java
 ┃ ┗ resources
 ┃ ┃ ┣ application.yml
 ┃ ┃ ┣ env.properties
 ┃ ┃ ┣ logback-spring.xml
 ┃ ┃ ┣ db
 ┃ ┃ ┃ ┗ findex_schema.sql
 ┃ ┃ ┗ static
 ┃ ┃ ┃ ┣ favicon.ico
 ┃ ┃ ┃ ┣ index.html
 ┃ ┃ ┃ ┗ assets
 ┃ ┃ ┃ ┃ ┣ index-CGZC7fCi.js
 ┃ ┃ ┃ ┃ ┗ index-Dtn62Xmo.css
 ┗ test
 ┃ ┗ java
 ┃ ┃ ┗ com
 ┃ ┃ ┃ ┗ example
 ┃ ┃ ┃ ┃ ┗ findex
 ┃ ┃ ┃ ┃ ┃ ┗ FindexApplicationTests.java
```



## **팀 노션 링크**
[https://www.notion.so/Findex_6team-264911ad8de0804f8c9efc0f8251c357](https://www.notion.so/Findex_6team-264911ad8de0804f8c9efc0f8251c357?pvs=21)



## **구현 홈페이지**
https://findex-team6.up.railway.app/



## **프로젝트 발표자료**
[findex_6team_발표자료.pdf](https://github.com/user-attachments/files/22373540/findex_6team_.pdf)
