package com.kropotov.lovehatebackend.db.dao.reactions

import com.kropotov.lovehatebackend.db.dao.DatabaseSingleton.dbQuery
import com.kropotov.lovehatebackend.db.models.*
import com.kropotov.lovehatebackend.utilities.mapToString
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime

class OpinionReactionDAOFacadeImpl : OpinionReactionDAOFacade {
    override suspend fun getReaction(userId: Int, opinionId: Int, type: ReactionType): OpinionReaction? = dbQuery {
        OpinionReactions
            .selectAll()
            .where {
                (OpinionReactions.userId eq userId) and (OpinionReactions.opinionId eq opinionId) and
                        (OpinionReactions.type eq type)
            }
            .singleOrNull()
            ?.let(::resultRowToReaction)
    }

    override suspend fun getReactions(opinionId: Int, userId: Int?): List<OpinionReaction> = dbQuery {
        OpinionReactions
            .selectAll()
            .where {
                val filterByUserId = if (userId != null) OpinionReactions.userId eq userId else Op.TRUE
                (OpinionReactions.opinionId eq opinionId) and filterByUserId
            }
            .map(::resultRowToReaction)
    }

    override suspend fun upsertReaction(userId: Int, opinionId: Int, type: ReactionType) {
        dbQuery {
            OpinionReactions.upsert {
                it[this.userId] = userId
                it[this.opinionId] = opinionId
                it[this.type] = type
                it[this.isNotified] = false
                it[this.date] = LocalDateTime.now()
            }
        }
    }

    override suspend fun deleteReaction(userId: Int, opinionId: Int): Boolean = dbQuery {
        OpinionReactions.deleteWhere {
            (OpinionReactions.userId eq userId) and (OpinionReactions.opinionId eq opinionId)
        } > 0
    }

    override suspend fun findUnreadNotifications(userId: Int) = dbQuery {
        OpinionReactions
            .innerJoin(Opinions, { opinionId }, { id })
            .innerJoin(Users, { OpinionReactions.userId }, { id })
            .select(
                OpinionReactions.userId,
                OpinionReactions.opinionId, Users.username, OpinionReactions.type, Opinions.text, Opinions.createdAt)
            .where {
                val isUnseen = OpinionReactions.isNotified eq Op.FALSE
                val filterByCurrentUser = Opinions.userId eq userId
                val itIsNotTheSameUser = OpinionReactions.userId neq Opinions.userId
                filterByCurrentUser and isUnseen and itIsNotTheSameUser
            }
            .map(::resultRowToNotification)
    }

    override suspend fun markAsRead(userId: Int, opinionId: Int) {
        dbQuery {
            OpinionReactions.update(where = {
                (OpinionReactions.userId eq userId) and (OpinionReactions.opinionId eq opinionId)
            }, body = {
                it[this.isNotified] = true
            })
        }
    }

    private fun resultRowToReaction(row: ResultRow) = OpinionReaction(
        userId = row[OpinionReactions.userId],
        opinionId = row[OpinionReactions.opinionId],
        date = row[OpinionReactions.date].mapToString(),
        type = row[OpinionReactions.type]
    )

    private fun resultRowToNotification(row: ResultRow) = NotificationReaction(
        userIdWhoFired = row[OpinionReactions.userId],
        opinionId = row[OpinionReactions.opinionId],
        date = row[Opinions.createdAt].mapToString(),
        sourceText = row[Opinions.text],
        type = row[OpinionReactions.type],
        who = row[Users.username]
    )
}