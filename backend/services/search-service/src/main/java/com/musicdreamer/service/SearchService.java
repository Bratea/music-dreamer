package com.musicdreamer.service;

import com.musicdreamer.dto.SearchRequest;
import com.musicdreamer.dto.SearchResult;

public interface SearchService {
    SearchResult search(SearchRequest request);
}
