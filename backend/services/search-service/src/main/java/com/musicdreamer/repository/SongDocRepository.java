package com.musicdreamer.repository;

import com.musicdreamer.entity.SongDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SongDocRepository extends ElasticsearchRepository<SongDoc, Long> {

    Page<SongDoc> findByNameContainingOrSingerNameContainingOrLyricsContaining(
            String name, String singerName, String lyrics, Pageable pageable);
}
