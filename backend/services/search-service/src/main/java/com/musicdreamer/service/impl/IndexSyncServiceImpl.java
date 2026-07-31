package com.musicdreamer.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
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
        // 使用 bulk API 一次性提交，避免 N 次 HTTP 往返
        BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();
        for (SongDoc song : songs) {
            bulkBuilder.operations(op -> op.index(idx -> idx
                    .index("song")
                    .id(String.valueOf(song.getSongId()))
                    .document(song)));
        }
        try {
            BulkResponse response = esClient.bulk(bulkBuilder.build());
            if (response.errors()) {
                throw new RuntimeException("ES bulk index has errors");
            }
        } catch (IOException e) {
            throw new RuntimeException("ES batch index failed: " + e.getMessage(), e);
        }
    }
}
