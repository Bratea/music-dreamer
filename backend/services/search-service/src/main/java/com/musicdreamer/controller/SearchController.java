package com.musicdreamer.controller;

import com.musicdreamer.common.CommonResult;
import com.musicdreamer.dto.SearchRequest;
import com.musicdreamer.dto.SearchResult;
import com.musicdreamer.service.SearchService;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/search")
@Tag(name = "搜索服务", description = "Elasticsearch 全文检索")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    @Operation(summary = "全文搜索", description = "支持歌曲名、歌手、歌词多字段匹配")
    public CommonResult<SearchResult> search(SearchRequest request) {
        return CommonResult.success(searchService.search(request));
    }
}
