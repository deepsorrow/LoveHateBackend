package com.kropotov.lovehatebackend.db.dao.reactions

import com.kropotov.lovehatebackend.db.models.Media
import com.kropotov.lovehatebackend.db.models.MediaType
import com.kropotov.lovehatebackend.db.models.Reaction
import com.kropotov.lovehatebackend.db.models.ReactionType

interface ReactionsDAOFacade {
    suspend fun getOpinionReactions(opinionId: Int, userId: Int? = null): List<Reaction>
    suspend fun getCommentsReactions(commentId: Int, userId: Int? = null): List<Reaction>

    suspend fun addReaction(userId: Int, opinionId: Int, commentId: Int, type: ReactionType): Reaction?

    suspend fun deleteReaction(id: Int): Boolean
}