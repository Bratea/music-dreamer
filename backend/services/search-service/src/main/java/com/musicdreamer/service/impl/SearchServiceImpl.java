package com.musicdreamer.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.musicdreamer.dto.SearchRequest;
import com.musicdreamer.dto.SearchResult;
import com.musicdreamer.dto.SearchResult.SongItem;
import com.musicdreamer.entity.SongDoc;
import com.musicdreamer.repository.SongDocRepository;
import com.musicdreamer.service.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    private final SongDocRepository songDocRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public SearchServiceImpl(SongDocRepository songDocRepository, ElasticsearchOperations elasticsearchOperations) {
        this.songDocRepository = songDocRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public SearchResult search(SearchRequest request) {
        String keyword = request.getKeyword();
        int page = Math.max(request.getPage(), 1);
        int size = request.getSize();

        List<SongDoc> results = Collections.emptyList();

        // Use native query with multi_match and wildcard for partial matching
        try {
            NativeQueryBuilder builder = NativeQuery.builder();
            // Combine multi_match with wildcard for better Chinese search
            Query boolQuery = Query.of(q -> q.bool(b -> b
                    .should(s -> s.multiMatch(mm -> mm
                            .fields("name^3", "singerName^2", "lyrics", "genre")
                            .query(keyword)
                            .fuzziness("AUTO")))
                    .should(s -> s.wildcard(w -> w
                            .field("name")
                            .value("*" + keyword + "*")))
                    .should(s -> s.wildcard(w -> w
                            .field("singerName")
                            .value("*" + keyword + "*")))
            ));
            builder.withQuery(boolQuery);
            builder.withPageable(PageRequest.of(0, 100));
            SearchHits<SongDoc> searchHits = elasticsearchOperations.search(builder.build(), SongDoc.class);
            results = searchHits.getSearchHits().stream()
                    .map(h -> h.getContent())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // ignore
        }

        // Fallback to repository method if native query returns empty
        if (results.isEmpty()) {
            try {
                Page<SongDoc> pageResult = songDocRepository.findByNameContainingOrSingerNameContainingOrLyricsContaining(
                        keyword, keyword, keyword, PageRequest.of(0, 100));
                results = pageResult.getContent();
            } catch (Exception e2) {
                // ignore
            }
        }

        SearchResult result = new SearchResult();
        result.setPage(page);
        result.setSize(size);
        result.setTotal(results.size());

        // Paginate
        int from = (page - 1) * size;
        int to = Math.min(from + size, results.size());
        List<SongDoc> pageData = from < to ? results.subList(from, to) : Collections.emptyList();

        result.setSongs(pageData.stream()
                .map(doc -> {
                    SongItem item = new SongItem();
                    item.setSongId(doc.getSongId());
                    item.setName(doc.getName());
                    item.setSingerName(doc.getSingerName());
                    item.setPlayCount(doc.getPlayCount());
                    return item;
                })
                .collect(Collectors.toList()));

        return result;
    }
}
