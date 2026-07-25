package com.musicdreamer.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Document(indexName = "song")
public class SongDoc {
    @Id
    private Long songId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String singerName;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String lyrics;

    @Field(type = FieldType.Keyword)
    private String genre;

    @Field(type = FieldType.Keyword)
    private String language;

    @Field(type = FieldType.Integer)
    private Integer playCount;

    @Field(type = FieldType.Date)
    private String releaseDate;
}
