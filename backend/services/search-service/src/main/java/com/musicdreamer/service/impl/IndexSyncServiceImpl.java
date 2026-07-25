package com.musicdreamer.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import com.musicdreamer.entity.SongDoc;
import com.musicdreamer.service.IndexSyncService;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class IndexSyncServiceImpl implements IndexSyncService {

    private final ElasticsearchClient esClient;

    public IndexSyncServiceImpl(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    @Override
    public void indexSong(SongDoc song) {
        try {
            esClient.index(i -> i.index("song").id(String.valueOf(song.getSongId())).document(song));
        } catch (IOException e) {
            throw new RuntimeException("ES index failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteSong(Long songId) {
        try {
            esClient.delete(DeleteRequest.of(d -> d.index("song").id(String.valueOf(songId))));
        } catch (IOException e) {
            throw new RuntimeException("ES delete failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void batchIndex(Iterable<SongDoc> songs) {
        try {
            for (SongDoc song : songs) {
                indexSong(song);
            }
        } catch (Exception e) {
            throw new RuntimeException("ES batch index failed: " + e.getMessage(), e);
        }
    }
}
