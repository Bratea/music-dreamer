package com.musicdreamer.event;

import com.musicdreamer.entity.SongDoc;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SongIndexEvent extends ApplicationEvent {
    private final SongDoc song;
    private final String eventType; // CREATE, UPDATE, DELETE

    public SongIndexEvent(Object source, SongDoc song, String eventType) {
        super(source);
        this.song = song;
        this.eventType = eventType;
    }
}
