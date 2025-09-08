package com.example.findex.common.openApi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenApiService {

    private final WebClient webClient;

    @Value("${api.url}")
    private String API_PATH;

    @Value("${api.key}")
    private String API_KEY;

    public String fetchIndexDataAsString(LocalDate date) {
        String baseDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        try {
            String jsonResponse = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(API_PATH)
                            // '+' 문자가 문제를 일으키므로, '%2B'로 직접 치환
                            .queryParam("serviceKey", API_KEY.replace("+", "%2B"))
                            .queryParam("resultType", "json")
                            .queryParam("basDt", baseDate)
                            .queryParam("numOfRows", "1000")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Successfully fetched API response for date: {}", baseDate);
            return jsonResponse;

        } catch (Exception e) {
            log.error("Failed to fetch index data from API for date: {}", baseDate, e);
            throw new RuntimeException("API call failed for date: " + baseDate, e);
        }
    }
}
