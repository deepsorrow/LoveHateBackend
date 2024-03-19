package com.kropotov.lovehatebackend.db.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

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
    val topicId = integer("topicId").nullable()
    val opinionId = integer("opinionId").nullable()
    val commentId = integer("commentId").nullable()
    val userId = integer("userId")
    val date = datetime("date")

    override val primaryKey = PrimaryKey(id)
}