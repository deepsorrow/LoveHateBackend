package com.kropotov.lovehatebackend.db.models

import com.kropotov.lovehatebackend.routes.BLANK_AVATARS_ROOT
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

data class User(
    val id: Int,
    val username: String,
    val password: String,
    val passwordHash: String,
    val email: String?,
    val role: UserRole,
    val createdAt: String
)

data class UserStatistics(
    val id: Int,
    var photoUrl: String,
    val username: String,
    val score: Int,
    val scoreTitle: UserScoreTitle,
    val topicsCount: Int,
    val opinionsCount: Int,
    val opinionPercent: Int,
    val type: OpinionType,
    val position: Int
) {

    init {
        photoUrl = photoUrl.ifBlank { BLANK_AVATARS_ROOT + (id % 9).toString() } + ".png"
    }
}

object Users : Table() {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 70)
    val password = varchar("password", 200)
    val passwordHash = varchar("password_hash", 200)
    val passwordUpdatedAt = datetime("password_updated_at").nullable()
    val photoUrl = varchar("photo_url", 200).nullable()
    val email = varchar("email", 60).nullable()
    val role = enumeration<UserRole>("role")
    val disabled = bool("disabled")
    val lastLoginAt = datetime("last_login_at")
    val createdAt = datetime("date")

    override val primaryKey = PrimaryKey(id, username)
}