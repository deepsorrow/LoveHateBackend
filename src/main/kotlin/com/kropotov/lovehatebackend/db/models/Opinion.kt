package com.kropotov.lovehatebackend.db.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

enum class OpinionType {
    LOVE,
    INDIFFERENCE,
    HATE
}

data class OpinionTypeCount(
    val opinionType: OpinionType,
    val count: Int
)

data class Opinion(
    val id: Int = 0,
    val topicId: Int,
    val userId: Int,
    val text: String,
    val type: OpinionType,
    val createdAt: String
)

data class OpinionListItem(
    val id: Int,
    val username: String,
    val topicTitle: String,
    val text: String,
    val type: OpinionType,
    val date: String,
    val likeCount: Int = 0,
    val dislikeCount: Int = 0,
    val messagesCount: Int = 0,
    val isFavorite: Boolean = false,
    val isLiked: Boolean = false,
    val isDisliked: Boolean = false
)

data class OpinionListResponse(
    val totalPages: Int,
    val results: List<OpinionListItem>
)

object Opinions : Table() {
    val id = integer("id").autoIncrement()
    val topicId = integer("topicId")
    val userId = integer("userId")
    val text = text("text")
    val type = enumeration<OpinionType>("type")
    val createdAt = datetime("createdAt")

    override val primaryKey = PrimaryKey(id)
}