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

object Comments : Table() {
    val id = integer("id").autoIncrement()
    val userId = integer("userId")
    val opinionId = integer("opinionId")
    val text = varchar("text", 128)
    val createdAt = datetime("createdAt")

    override val primaryKey = PrimaryKey(id)
}