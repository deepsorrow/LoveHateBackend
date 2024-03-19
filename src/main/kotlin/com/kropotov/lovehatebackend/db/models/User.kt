package com.kropotov.lovehatebackend.db.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

enum class UserRole {
    USER,
    MODERATOR,
    ADMIN
}

enum class UserScoreTitle(
    val scoreThreshold: Int
) {
    SURVEYOR(0),
    INSTIGATOR(100),
    ENLIGHTENED(500),
    JUDGE(1000),
    FORSETI(3000);

    companion object {
        fun getTitle(score: Int) =
            when {
                score >= FORSETI.scoreThreshold -> FORSETI
                score >= JUDGE.scoreThreshold -> JUDGE
                score >= ENLIGHTENED.scoreThreshold -> ENLIGHTENED
                score >= INSTIGATOR.scoreThreshold -> INSTIGATOR
                else -> SURVEYOR
            }
    }
}

data class UserStatisticsDetailed(
    val userId: Int,
    val username: String,
    val score: Int,
    val scoreTitle: UserScoreTitle,
    val topicsCount: Int,
    val opinionsCount: Int,
    val commentsCount: Int,
    val percent: Int,
    val type: OpinionType
)

data class User(
    val id: Int,
    val username: String,
    val password: String,
    val passwordHash: String,
    val email: String?,
    val score: Int,
    val role: UserRole,
    val createdAt: String
)

object Users : Table() {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 70)
    val password = varchar("password", 200)
    val passwordHash = varchar("passwordHash", 200)
    val passwordUpdatedAt = datetime("passwordUpdatedAt").nullable()
    val photoUrl = varchar("photoUrl", 200).nullable()
    val email = varchar("email", 60).nullable()
    val role = enumeration<UserRole>("role")
    val disabled = bool("disabled")
    val lastLoginAt = datetime("lastLoginAt")
    val createdAt = datetime("date")

    override val primaryKey = PrimaryKey(id)
}