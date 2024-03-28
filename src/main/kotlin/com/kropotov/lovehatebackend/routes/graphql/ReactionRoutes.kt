package com.kropotov.lovehatebackend.routes.graphql

import com.apurebase.kgraphql.Context
import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import com.kropotov.lovehatebackend.db.dao.reactions.OpinionReactionDAOFacade
import com.kropotov.lovehatebackend.db.models.OpinionReaction
import com.kropotov.lovehatebackend.db.models.ReactionType
import com.kropotov.lovehatebackend.routes.models.UpdateResponse
import com.kropotov.lovehatebackend.utilities.getUserId
import org.kodein.di.DI
import org.kodein.di.instance

fun SchemaBuilder.reactionRoutes(kodein: DI) {

    val reactionsDao by kodein.instance<OpinionReactionDAOFacade>()

    type<OpinionReaction> {
        description = "Reaction to opinion or comment: like/dislike"
    }

    enum<ReactionType> {
        description = "Like or dislike"
    }

    mutation("updateOpinionReaction") {
        description = "Adds/updates/deletes reaction for opinion"
        resolver { context: Context, opinionId: Int, type: ReactionType ->
            val isExist = reactionsDao.getReaction(context.getUserId(), opinionId, type) != null
            if (isExist) {
                reactionsDao.deleteReaction(context.getUserId(), opinionId)
            } else {
                reactionsDao.upsertReaction(context.getUserId(), opinionId, type)
            }
            UpdateResponse(true)
        }
    }

    // TODO Comments feature
//    mutation("updateCommentReaction") {
//        description = "Adds/updates/deletes reaction for comment"
//        resolver { context: Context, commentId: Int, type: ReactionType? ->
//            if (type != null) {
//                reactionsDao.upsertReaction(context.getUserId(), null, commentId, type)
//            } else {
//                reactionsDao.deleteReaction(context.getUserId(), null, commentId)
//            }
//            UpdateResponse(true)
//        }
//    }
}