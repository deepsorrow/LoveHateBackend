package com.kropotov.lovehatebackend.db.dao.feedback


interface FeedbackDAOFacade {

    suspend fun insertFeedback(userId: Int, text: String): Boolean
}