package com.kropotov.lovehatebackend.routes.graphql

import com.apurebase.kgraphql.Context
import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import com.kropotov.lovehatebackend.db.dao.favorites.FavoritesDAOFacade
import com.kropotov.lovehatebackend.routes.models.UpdateResponse
import com.kropotov.lovehatebackend.utilities.getUserId
import org.kodein.di.DI
import org.kodein.di.instance

fun SchemaBuilder.favoriteRoutes(kodein: DI) {
    val favoritesDao by kodein.instance<FavoritesDAOFacade>()

    type<UpdateResponse> {
        description = "Response on mutations"
    }

    mutation("updateTopicFavorite") {
        description = "Adds/updates/deletes reaction for topic"
        resolver { context: Context, topicId: Int ->

            val isFavorite = favoritesDao.getFavorite(context.getUserId(), topicId, null, null) != null
            if (isFavorite) {
                favoritesDao.deleteFavorite(context.getUserId(), topicId, null, null)
            } else {
                favoritesDao.insertFavorite(context.getUserId(), topicId, null, null)
            }
            UpdateResponse(!isFavorite)
        }
    }

    mutation("updateOpinionFavorite") {
        description = "Adds/updates/deletes reaction for opinion"
        resolver { context: Context, opinionId: Int ->

            val isFavorite = favoritesDao.getFavorite(context.getUserId(), null, opinionId, null) != null
            if (isFavorite) {
                favoritesDao.deleteFavorite(context.getUserId(), null, opinionId, null)
            } else {
                favoritesDao.insertFavorite(context.getUserId(), null, opinionId, null)
            }
            UpdateResponse(!isFavorite)
        }
    }

    mutation("updateCommentFavorite") {
        description = "Adds/updates/deletes reaction for comment"
        resolver { context: Context, commentId: Int ->

            val isFavorite = favoritesDao.getFavorite(context.getUserId(), null, null, commentId) != null
            if (isFavorite) {
                favoritesDao.deleteFavorite(context.getUserId(), null, null, commentId)
            } else {
                favoritesDao.insertFavorite(context.getUserId(), null, null, commentId)
            }
            UpdateResponse(!isFavorite)
        }
    }
}