package com.kropotov.lovehatebackend.db.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

enum class OpinionType {
    LOVE,
    INDIFFERENCE,
    HATE
}

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
    val attachmentUrls: List<String> = listOf(),
    val likeCount: String = "0",
    val dislikeCount: String = "0",
    val isFavorite: Boolean = false,
    val isLiked: Boolean = false,
    val isDisliked: Boolean = false,
    val position: Int
)

object Opinions : Table() {
    val id = integer("id").autoIncrement()
    val topicId = integer("topic_id")
    val userId = integer("user_id")
    val text = text("text")
    val type = enumeration<OpinionType>("type")
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}