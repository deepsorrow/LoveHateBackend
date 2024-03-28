package com.kropotov.lovehatebackend.db.models

import org.jetbrains.exposed.sql.Table

enum class MediaType {
    GIF,
    IMAGE,
    VIDEO,
    UNKNOWN
}

enum class MediaSource {
    GALLERY,
    UNSPLASH,
    SEARCH
}

data class Media(
    val id: Int,
    val topicId: Int?,
    val opinionId: Int?,
    val commentId: Int?,
    val srcPath: String,
    val source: MediaSource,
    val type: MediaType,
)

object Multimedia : Table() {
    val id = integer("id").autoIncrement()
    val topicId = integer("topic_id").nullable()
    val opinionId = integer("opinion_id").nullable()
    val commentId = integer("comment_id").nullable()
    val srcPath = varchar("src_path", 256)
    val mediaSource = enumeration<MediaSource>("source")
    val type = enumeration<MediaType>("type")

    override val primaryKey = PrimaryKey(id)
}