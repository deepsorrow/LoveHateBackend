package com.kropotov.lovehatebackend.db.models

import com.kropotov.lovehatebackend.db.models.Users.autoIncrement
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

enum class ReactionType {
    LIKE,
    DISLIKE
}

data class Reaction(
    val id: Int,
    val userId: Int,
    val opinionId: Int?,
    val commentId: Int?,
    val type: ReactionType
)

object Reactions : Table() {
    val id = integer("id").autoIncrement()
    val userId = integer("userId")
    val opinionId = integer("opinionId")
    val commentId = integer("commentId")
    val type = enumeration<ReactionType>("type")

    override val primaryKey = PrimaryKey(id)
}