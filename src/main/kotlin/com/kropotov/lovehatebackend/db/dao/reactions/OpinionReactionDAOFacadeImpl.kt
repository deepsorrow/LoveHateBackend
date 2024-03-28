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

    override suspend fun upsertReaction(userId: Int, opinionId: Int, type: ReactionType): OpinionReaction? = dbQuery {
        val insertOpinion = OpinionReactions.upsert {
            it[this.userId] = userId
            it[this.opinionId] = opinionId
            it[this.type] = type
            it[this.date] = LocalDateTime.now()
        }
        insertOpinion.resultedValues?.singleOrNull()?.let(::resultRowToReaction)
    }

    override suspend fun deleteReaction(userId: Int, opinionId: Int): Boolean = dbQuery {
        OpinionReactions.deleteWhere {
            (OpinionReactions.userId eq userId) and (OpinionReactions.opinionId eq opinionId)
        } > 0
    }

    private fun resultRowToReaction(row: ResultRow) = OpinionReaction(
        userId = row[OpinionReactions.userId],
        opinionId = row[OpinionReactions.opinionId],
        date = row[OpinionReactions.date].mapToString(),
        type = row[OpinionReactions.type]
    )

}