package com.kropotov.lovehatebackend.db.dao.reactions

import com.kropotov.lovehatebackend.db.models.Reaction
import com.kropotov.lovehatebackend.db.models.ReactionType

interface ReactionsDAOFacade {
    suspend fun getOpinionReactions(opinionId: Int, userId: Int? = null): List<Reaction>
    suspend fun getCommentsReactions(commentId: Int, userId: Int? = null): List<Reaction>

    suspend fun upsertReaction(userId: Int, opinionId: Int?, commentId: Int?, type: ReactionType): Reaction?

    suspend fun deleteReaction(userId: Int, opinionId: Int?, commentId: Int?): Boolean
}