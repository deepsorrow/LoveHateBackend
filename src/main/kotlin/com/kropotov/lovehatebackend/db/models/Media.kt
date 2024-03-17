package com.kropotov.lovehatebackend.db.models

import org.jetbrains.exposed.sql.Table

enum class MediaType {
    GIF,
    IMAGE,
    VIDEO,
    UNKNOWN
}

data class Media(
    val id: Int,
    val topicId: Int,
    val opinionId: Int,
    val commentId: Int,
    val srcPath: String,
    val type: MediaType,
)

object Multimedia : Table() {
    val id = integer("id").autoIncrement()
    val topicId = integer("topicId")
    val opinionId = integer("opinionId")
    val commentId = integer("commentId")
    val srcPath = varchar("srcPath", 256)
    val type = enumeration<MediaType>("type")

    override val primaryKey = PrimaryKey(id)
}