package com.kropotov.lovehatebackend.db.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

data class Topic(
    val id: Int,
    val userId: Int,
    val title: String,
    val createdAt: String
)

data class TopicPage(
    val id: Int,
    val title: String,
    val opinionsCount: Int,
    val opinionType: OpinionType,
    val percent: Int,
    val author: String,
    val authorOpinionType: OpinionType,
    val isFavorite: Boolean,
    val createdAt: String
)

data class TopicsListResponse(
    val totalPages: Int,
    val results: List<Topic>
)

enum class TopicsSortType {
    RECENT,
    NEW,
    POPULAR,
    MOST_LOVED,
    MOST_HATED,
    FAVORITES,
    BY_USER_ID
}

object Topics : Table() {
    val id = integer("id").autoIncrement()
    val userId = integer("userId")
    val title = varchar("title", 128)
    val createdAt = datetime("createdAt")

    override val primaryKey = PrimaryKey(id)
}