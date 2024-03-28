package com.kropotov.lovehatebackend.db.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object Feedbacks : Table() {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id")
    val text = text("text")
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}
