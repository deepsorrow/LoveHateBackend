package com.kropotov.lovehatebackend.db.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

/**
 * It is best to save [opinionsCount], [opinionType] and [percent] than calculate it each time
 * through joining multiple tables at each list request.
 */
data class Topic(
    val id: Int,
    val userId: Int,
    val title: String,
    var opinionsCount: Int,
    var opinionType: OpinionType,
    var percent: Int,
    var loveIndex: Double,
    val date: String
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
    val date: String
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
    val opinionsCount = integer("opinionsCount")
    val opinionType = enumeration<OpinionType>("opinionType")
    val loveIndex = double("loveIndex")
    val percent = integer("percent")
    val date = datetime("date")

    override val primaryKey = PrimaryKey(id)
}