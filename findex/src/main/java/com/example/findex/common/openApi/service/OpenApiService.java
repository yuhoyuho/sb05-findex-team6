package com.example.findex.common.openApi.service;

import com.example.findex.common.openApi.dto.IndexApiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:env.properties")
@Slf4j
public class OpenApiService {

    private final WebClient webClient;

    @Value("${api.url}")
    private String API_URL;

    @Value("${api.key}")
    private String API_KEY;

    /**
     * 특정 날짜의 주식 지수 데이터를 OpenAPI로부터 가져와 DTO로 반환합니다.
     * @param date 조회할 날짜
     * @return API 응답 전체를 담은 IndexApiResponseDto 객체
     */
    public IndexApiResponseDto fetchStockData(LocalDate date) {
        String baseDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // UriComponentsBuilder를 사용해 파라미터를 안전하게 추가하고 URL을 생성합니다.
        URI uri = UriComponentsBuilder
                .fromUriString(API_URL)
                .queryParam("serviceKey", API_KEY)
                .queryParam("resultType", "json")
                .queryParam("basDt", baseDate)
                .queryParam("numOfRows", "1000") // 충분한 개수로 설정
                .build(true) // 키에 포함된 '+' 같은 특수문자가 깨지지 않도록 인코딩
                .toUri();

        log.info("Requesting API for date '{}' with URL: {}", baseDate, uri);

        try {
            // WebClient를 사용해 API를 호출하고, 응답을 IndexApiResponseDto로 자동 변환합니다.
            IndexApiResponseDto responseDto = webClient.get()
                    .uri(uri)
                    .retrieve() // 응답 수신
                    .bodyToMono(IndexApiResponseDto.class) // 응답 본문을 DTO로 변환
                    .block(); // 비동기 응답을 동기적으로 대기

            if (responseDto != null && "00".equals(responseDto.getResponse().getHeader().getResultCode())) {
                log.info("Successfully fetched API data for date: {}", baseDate);
            } else {
                String errorMsg = responseDto != null ? responseDto.getResponse().getHeader().getResultMsg() : "Response is null";
                log.error("API call failed for date: {}. Reason: {}", baseDate, errorMsg);
            }

            return responseDto;

        } catch (Exception e) {
            log.error("An unexpected error occurred while fetching API data for date: {}", baseDate, e);
            // API 호출 자체에 실패하면 null을 반환하거나, 비어있는 DTO를 반환할 수 있습니다.
            return null;
        }
    }

}
