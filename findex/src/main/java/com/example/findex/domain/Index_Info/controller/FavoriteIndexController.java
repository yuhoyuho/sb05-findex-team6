package com.example.findex.domain.Index_Info.controller;

import com.example.findex.domain.Index_Info.dto.FavoriteIndexDto;
import com.example.findex.domain.Index_Info.service.FavoriteIndexService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorites")
public class FavoriteIndexController {

    private final FavoriteIndexService favoriteIndexService;

    @GetMapping("/summary")
    public ResponseEntity<List<FavoriteIndexDto>> getFavoriteIndexSummaries() {
        List<FavoriteIndexDto> summaries = favoriteIndexService.findFavoriteIndexSummaries();
        return ResponseEntity.ok(summaries);
    }
}