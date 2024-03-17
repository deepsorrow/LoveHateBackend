package com.kropotov.lovehatebackend.db.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

enum class UserRole {
    BANNED,
    DEFAULT,
    MODERATOR,
    ADMIN
}

data class User(
    val id: Int,
    val username: String,
    val password: String,
    val email: String,
    val score: Int,
    val role: UserRole,
    val date: String
)

object Users : Table() {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 70)
    val password = varchar("password", 30)
    val email = varchar("email", 100)
    val score = integer("score")
    val role = enumeration<UserRole>("role")
    val date = datetime("date")

    override val primaryKey = PrimaryKey(id)
}