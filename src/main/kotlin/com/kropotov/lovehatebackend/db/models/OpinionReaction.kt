package com.kropotov.lovehatebackend.db.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

enum class ReactionType {
    LIKE,
    DISLIKE
}

data class OpinionReaction(
    val userId: Int,
    val opinionId: Int,
    val type: ReactionType,
    val date: String
)

data class NotificationReaction(
    val userIdWhoFired: Int,
    val opinionId: Int,
    val who: String,
    val sourceText: String,
    val date: String,
    val type: ReactionType
)

object OpinionReactions : Table() {
    val userId = integer("user_id")
    val opinionId = integer("opinion_id")
    val type = enumeration<ReactionType>("type")
    val isNotified = bool("isNotified")
    val date = datetime("date")

    override val primaryKey = PrimaryKey(userId, opinionId)
}