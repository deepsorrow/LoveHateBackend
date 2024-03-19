package com.kropotov.lovehatebackend.routes.graphql

import com.apurebase.kgraphql.Context
import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import com.kropotov.lovehatebackend.db.dao.reactions.ReactionsDAOFacadeImpl
import com.kropotov.lovehatebackend.db.models.Reaction
import com.kropotov.lovehatebackend.db.models.ReactionType
import com.kropotov.lovehatebackend.utilities.getUserId

fun SchemaBuilder.reactionRoutes() {

    val reactionsDao = ReactionsDAOFacadeImpl()

    type<Reaction> {
        description = "Reaction to opinion or comment: like/dislike"
    }

    enum<ReactionType> {
        description = "Like or dislike"
    }

    mutation("updateReaction") {
        description = "Adds/updates/deletes reaction to opinion or comment"
        resolver { context: Context, opinionId: Int?, commentId: Int?, type: ReactionType? ->
            if (type != null) {
                reactionsDao.upsertReaction(context.getUserId(), opinionId, commentId, type)
            } else {
                reactionsDao.deleteReaction(context.getUserId(), opinionId, commentId)
            }
            true
        }
    }
}