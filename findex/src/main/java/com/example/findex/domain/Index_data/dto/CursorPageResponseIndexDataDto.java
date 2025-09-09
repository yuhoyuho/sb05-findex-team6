package com.example.findex.domain.Index_data.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CursorPageResponseIndexDataDto {
    private List<IndexDataDto> content;
    private String nextCursor;
    private Long nextIdAfter;
    private Integer size;
    private Long totalElements;
    private Boolean hasNext;

}
