package com.musicdreamer.service;

import com.musicdreamer.entity.SongDoc;

public interface IndexSyncService {
    void indexSong(SongDoc song);
    void deleteSong(Long songId);
    void batchIndex(Iterable<SongDoc> songs);
}
