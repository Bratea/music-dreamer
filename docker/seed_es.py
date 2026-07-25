#!/usr/bin/env python3
"""
Elasticsearch 初始数据灌入脚本
从 MySQL 读取 song + singer 数据，写入 ES 的 song 索引

用法:
  python3 docker/seed_es.py              # 使用默认连接参数
  python3 docker/seed_es.py --reset      # 先删除索引再重建

依赖: pip install pymysql elasticsearch
"""

import argparse
import sys

MYSQL_HOST = "127.0.0.1"
MYSQL_PORT = 3307
MYSQL_USER = "music"
MYSQL_PASSWORD = "music123456"
MYSQL_DB = "music_dreamer"

ES_HOST = "http://127.0.0.1:9200"
ES_INDEX = "song"

# 歌曲数据查询（JOIN singer 表获取歌手名）
SONG_QUERY = """
SELECT
    s.song_id,
    s.name,
    IFNULL(si.name, '')  AS singer_name,
    IFNULL(s.lyrics, '') AS lyrics,
    IFNULL(s.genre, '')  AS genre,
    IFNULL(s.language, '') AS language,
    IFNULL(s.play_count, 0) AS play_count,
    IFNULL(DATE_FORMAT(s.release_date, '%Y-%m-%d'), '') AS release_date
FROM song s
LEFT JOIN singer si ON s.singer_id = si.singer_id
WHERE s.status = 1
"""

INDEX_MAPPINGS = {
    "settings": {
        "number_of_shards": 1,
        "number_of_replicas": 0,
        "analysis": {
            "analyzer": {
                "ik_smart_pinyin": {
                    "type": "custom",
                    "tokenizer": "ik_smart",
                    "filter": ["lowercase"]
                }
            }
        }
    },
    "mappings": {
        "properties": {
            "songId":      {"type": "long"},
            "name":        {"type": "text", "analyzer": "standard",
                            "fields": {"keyword": {"type": "keyword"}}},
            "singerName":  {"type": "text", "analyzer": "standard",
                            "fields": {"keyword": {"type": "keyword"}}},
            "lyrics":      {"type": "text", "analyzer": "standard"},
            "genre":       {"type": "keyword"},
            "language":    {"type": "keyword"},
            "playCount":   {"type": "integer"},
            "releaseDate": {"type": "keyword"}
        }
    }
}


def get_mysql_data():
    import pymysql
    conn = pymysql.connect(
        host=MYSQL_HOST, port=MYSQL_PORT,
        user=MYSQL_USER, password=MYSQL_PASSWORD,
        database=MYSQL_DB, charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor
    )
    try:
        with conn.cursor() as cur:
            cur.execute(SONG_QUERY)
            return cur.fetchall()
    finally:
        conn.close()


def seed(reset=False):
    from elasticsearch import Elasticsearch
    es = Elasticsearch([ES_HOST])

    # 等待 ES 就绪
    import time
    for _ in range(30):
        if es.ping():
            break
        print("等待 Elasticsearch 启动...", flush=True)
        time.sleep(2)
    else:
        print("ERROR: 无法连接 Elasticsearch", file=sys.stderr)
        sys.exit(1)

    if reset:
        if es.indices.exists(index=ES_INDEX):
            es.indices.delete(index=ES_INDEX)
            print(f"已删除旧索引: {ES_INDEX}")

    # 创建索引
    if not es.indices.exists(index=ES_INDEX):
        es.indices.create(index=ES_INDEX, body=INDEX_MAPPINGS)
        print(f"已创建索引: {ES_INDEX}")

    # 读取 MySQL 数据
    rows = get_mysql_data()
    if not rows:
        print("WARNING: MySQL 中没有符合条件的歌曲数据")
        return
    print(f"从 MySQL 读取到 {len(rows)} 首歌曲", flush=True)

    # 写入 ES
    from elasticsearch.helpers import bulk
    actions = []
    for row in rows:
        actions.append({
            "_index": ES_INDEX,
            "_id": row["song_id"],
            "_source": {
                "songId": row["song_id"],
                "name": row["name"],
                "singerName": row["singer_name"],
                "lyrics": row["lyrics"],
                "genre": row["genre"],
                "language": row["language"],
                "playCount": row["play_count"],
                "releaseDate": row["release_date"],
            }
        })

    success, errors = bulk(es, actions, refresh="wait_for")
    print(f"成功写入 {success} 条文档到 ES")
    if errors:
        print(f"失败 {errors} 条", file=sys.stderr)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="灌入歌曲数据到 Elasticsearch")
    parser.add_argument("--reset", action="store_true", help="删除旧索引后重建")
    args = parser.parse_args()
    seed(reset=args.reset)
