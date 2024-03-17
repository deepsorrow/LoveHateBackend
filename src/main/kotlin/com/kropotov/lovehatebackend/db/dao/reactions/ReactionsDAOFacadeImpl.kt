package com.kropotov.lovehatebackend.db.dao.reactions

import com.kropotov.lovehatebackend.db.dao.DatabaseSingleton.dbQuery
import com.kropotov.lovehatebackend.db.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class ReactionsDAOFacadeImpl : ReactionsDAOFacade {
    private fun resultRowToReaction(row: ResultRow) = Reaction(
        id = row[Reactions.id],
        userId = row[Reactions.userId],
        opinionId = row[Reactions.opinionId],
        commentId = row[Reactions.commentId],
        type = row[Reactions.type],
    )

    override suspend fun getOpinionReactions(opinionId: Int, userId: Int?): List<Reaction> = dbQuery {
        Reactions
            .select {
                val filterByUserId = if (userId != null) Reactions.userId eq userId else Op.TRUE

                (Reactions.opinionId eq opinionId) and filterByUserId
            }
            .map(::resultRowToReaction)
    }

    override suspend fun getCommentsReactions(commentId: Int, userId: Int?): List<Reaction> = dbQuery {
        Reactions
            .select {
                val filterByUserId = if (userId != null) Reactions.userId eq userId else Op.TRUE

                (Reactions.commentId eq commentId) and filterByUserId
            }
            .map(::resultRowToReaction)
    }

    override suspend fun addReaction(userId: Int, opinionId: Int, commentId: Int, type: ReactionType): Reaction? = dbQuery {
        val insertOpinion = Reactions.insert {
            it[this.userId] = userId
            it[this.opinionId] = opinionId
            it[this.commentId] = commentId
            it[this.type] = type
        }
        insertOpinion.resultedValues?.singleOrNull()?.let(::resultRowToReaction)
    }

    override suspend fun deleteReaction(id: Int): Boolean = dbQuery {
        Reactions.deleteWhere { Reactions.id eq id } > 0
    }

}