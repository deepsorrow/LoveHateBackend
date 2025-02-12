package com.kropotov.lovehatebackend.db.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

data class Comment(
    val id: Int,
    val userId: Int,
    val opinionId: Int,
    val text: String,
    val createdAt: String,
)

object Comments : Table("comments") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id")
    val opinionId = integer("opinion_id")
    val text = varchar("text", 128)
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}