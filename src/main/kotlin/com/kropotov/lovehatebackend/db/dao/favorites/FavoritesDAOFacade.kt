package com.kropotov.lovehatebackend.db.dao.favorites

import com.kropotov.lovehatebackend.db.models.Favorite

interface FavoritesDAOFacade {

    suspend fun insertFavorite(userId: Int, topicId: Int?, opinionId: Int?, commentId: Int?): Favorite?

    suspend fun deleteFavorite(userId: Int, topicId: Int?, opinionId: Int?, commentId: Int?): Boolean
}