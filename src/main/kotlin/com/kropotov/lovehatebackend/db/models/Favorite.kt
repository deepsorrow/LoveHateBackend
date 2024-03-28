package com.kropotov.lovehatebackend.db.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

data class Favorite(
    val id: Int,
    val topicId: Int?,
    val opinionId: Int?,
    val commentId: Int?,
    val userId: Int,
    val date: String
)

object Favorites : Table() {
    val id = integer("id").autoIncrement()
    val topicId = integer("topic_id").nullable()
    val opinionId = integer("opinion_id").nullable()
    val commentId = integer("comment_id").nullable()
    val userId = integer("user_id")
    val date = datetime("date")

    override val primaryKey = PrimaryKey(id)
}