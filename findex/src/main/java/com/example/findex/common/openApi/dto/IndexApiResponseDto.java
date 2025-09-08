package com.example.findex.common.openApi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class IndexApiResponseDto {

    private Response response;

    @Getter
    @Setter
    @ToString
    public static class Response {
        private Header header;
        private Body body;
    }

    @Getter
    @Setter
    @ToString
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }

    @Getter
    @Setter
    @ToString
    public static class Body {
        private int numOfRows;
        private int pageNo;
        private int totalCount;
        private Items items;
    }

    @Getter
    @Setter
    @ToString
    public static class Items {
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        private List<Item> item;
    }

    @Getter
    @Setter
    @ToString
    public static class Item {
        @JsonProperty("basDt")
        private String baseDate; // 기준일자

        @JsonProperty("idxNm")
        private String indexName; // 지수명

        @JsonProperty("idxCsf")
        private String indexClassification; // 지수 분류

        @JsonProperty("basPntm")
        private String basePointTime; // 기준시점

        @JsonProperty("basIdx")
        private String baseIndex; // 기준지수

        @JsonProperty("epyItmsCnt")
        private int employedItemsCount; // 채용종목수

        @JsonProperty("clpr")
        private String closingPrice; // 종가

        @JsonProperty("vs")
        private String versus; // 대비

        @JsonProperty("fltRt")
        private String fluctuationRate; // 등락률

        @JsonProperty("mkp")
        private String marketPrice; // 시가

        @JsonProperty("hipr")
        private String highPrice; // 고가

        @JsonProperty("lopr")
        private String lowPrice; // 저가

        @JsonProperty("trqu")
        private String tradingQuantity; // 거래량

        @JsonProperty("trPrc")
        private String tradingPrice; // 거래대금

        @JsonProperty("lstgMrktTotAmt")
        private String marketTotalAmount; // 상장시가총액

        @JsonProperty("lsYrEdVsFltRg")
        private String lastYearEndVersusFluctuationRange; // 전년말대비 등락폭

        @JsonProperty("lsYrEdVsFltRt")
        private String lastYearEndVersusFluctuationRate; // 전년말대비 등락률

        @JsonProperty("yrWRcrdHgst")
        private String yearRecordHighest; // 연중최고

        @JsonProperty("yrWRcrdHgstDt")
        private String yearRecordHighestDate; // 연중최고일자

        @JsonProperty("yrWRcrdLwst")
        private String yearRecordLowest; // 연중최저

        @JsonProperty("yrWRcrdLwstDt")
        private String yearRecordLowestDate; // 연중최저일자
    }
}
