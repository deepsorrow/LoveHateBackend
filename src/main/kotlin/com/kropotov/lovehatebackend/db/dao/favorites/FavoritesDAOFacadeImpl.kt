package com.kropotov.lovehatebackend.db.dao.favorites

import com.kropotov.lovehatebackend.db.dao.DatabaseSingleton.dbQuery
import com.kropotov.lovehatebackend.db.models.*
import com.kropotov.lovehatebackend.utilities.mapToString
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime

class FavoritesDAOFacadeImpl : FavoritesDAOFacade {

    override suspend fun getFavorite(userId: Int, topicId: Int?, opinionId: Int?, commentId: Int?): Favorite? = dbQuery {
        Favorites
            .selectAll()
            .where {
                (Favorites.userId eq userId) and (Favorites.topicId eq topicId) and
                        (Favorites.opinionId eq opinionId) and (Favorites.commentId eq commentId)
            }
            .singleOrNull()
            ?.let(::resultRowToFavorite)
    }

    override suspend fun insertFavorite(userId: Int, topicId: Int?, opinionId: Int?, commentId: Int?): Favorite? = dbQuery {
        val insertOpinion = Favorites.insert {
            it[this.userId] = userId
            it[this.topicId] = topicId
            it[this.opinionId] = opinionId
            it[this.commentId] = commentId
            it[this.date] = LocalDateTime.now()
        }
        insertOpinion.resultedValues?.singleOrNull()?.let(::resultRowToFavorite)
    }

    override suspend fun deleteFavorite(userId: Int, topicId: Int?, opinionId: Int?, commentId: Int?): Boolean = dbQuery {
        Favorites.deleteWhere {
            (Favorites.userId eq userId) and (Favorites.topicId eq topicId) and (Favorites.opinionId eq opinionId) and (Favorites.commentId eq commentId)
        } > 0
    }

    private fun resultRowToFavorite(row: ResultRow) = Favorite(
        id = row[Favorites.id],
        userId = row[Favorites.userId],
        topicId = row[Favorites.topicId],
        opinionId = row[Favorites.opinionId],
        commentId = row[Favorites.commentId],
        date = row[Favorites.date].mapToString(),
    )
}