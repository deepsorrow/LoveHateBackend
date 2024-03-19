package com.kropotov.lovehatebackend.routes.graphql

import com.apurebase.kgraphql.Context
import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import com.kropotov.lovehatebackend.db.dao.favorites.FavoritesDAOFacadeImpl
import com.kropotov.lovehatebackend.utilities.getUserId

fun SchemaBuilder.favoriteRoutes() {

    val dao = FavoritesDAOFacadeImpl()

    mutation("updateFavorite") {
        description = "Adds/updates/deletes reaction to opinion or comment"
        resolver { context: Context, topicId: Int?, opinionId: Int?, commentId: Int?, isFavorite: Boolean ->

            if (isFavorite) {
                dao.insertFavorite(context.getUserId(), topicId, opinionId, commentId)
            } else {
                dao.deleteFavorite(context.getUserId(), topicId, opinionId, commentId)
            }
            true
        }
    }
}