package com.kropotov.lovehatebackend.db.dao.comments

import com.kropotov.lovehatebackend.db.models.Comment

interface CommentsDAOFacade {
    suspend fun getComment(id: Int): Comment?
    suspend fun addNewComment(opinionId: Int, userId: Int, text: String): Comment?

    suspend fun editComment(id: Int, title: String): Boolean

    suspend fun findMostPopularComments(page: Long): List<Comment>

    suspend fun findMostLovedComments(page: Long): List<Comment>

    suspend fun findMostHatedComments(page: Long): List<Comment>

    suspend fun findFavoriteComments(userId: Int, page: Long): List<Comment>

    suspend fun findUserComments(userId: Int, page: Long): List<Comment>

    suspend fun deleteComment(id: Int): Boolean
}