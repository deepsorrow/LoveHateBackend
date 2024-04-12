package com.kropotov.lovehatebackend.db.dao.reactions

import com.kropotov.lovehatebackend.db.models.NotificationReaction
import com.kropotov.lovehatebackend.db.models.OpinionReaction
import com.kropotov.lovehatebackend.db.models.ReactionType

interface OpinionReactionDAOFacade {

    suspend fun getReaction(userId: Int, opinionId: Int, type: ReactionType): OpinionReaction?

    suspend fun getReactions(opinionId: Int, userId: Int? = null): List<OpinionReaction>

    suspend fun upsertReaction(userId: Int, opinionId: Int, type: ReactionType)

    suspend fun findUnreadNotifications(userId: Int): List<NotificationReaction>

    suspend fun markAsRead(userId: Int, opinionId: Int)

    suspend fun deleteReaction(userId: Int, opinionId: Int): Boolean
}