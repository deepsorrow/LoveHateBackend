package com.kropotov.lovehatebackend.db.models

import com.kropotov.lovehatebackend.db.models.Users.autoIncrement
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

data class Favorite(
    val id: Int,
    val topicId: Int,
    val opinionId: Int,
    val commentId: Int,
    val userId: Int,
    val isFavorite: Boolean,
    val date: LocalDateTime
)

object Favorites : Table() {
    val id = integer("id").autoIncrement()
    val topicId = integer("topicId")
    val opinionId = integer("opinionId")
    val commentId = integer("commentId")
    val userId = integer("userId")
    val isFavorite = bool("isFavorite")
    val date = datetime("date")

    override val primaryKey = PrimaryKey(id)
}