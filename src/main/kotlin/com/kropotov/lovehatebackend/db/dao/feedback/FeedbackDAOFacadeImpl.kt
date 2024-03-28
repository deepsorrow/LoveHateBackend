package com.kropotov.lovehatebackend.db.dao.feedback

import com.kropotov.lovehatebackend.db.dao.DatabaseSingleton.dbQuery
import com.kropotov.lovehatebackend.db.models.Feedbacks
import org.jetbrains.exposed.sql.*
import java.time.LocalDateTime

class FeedbackDAOFacadeImpl : FeedbackDAOFacade {

    override suspend fun insertFeedback(userId: Int, text: String): Boolean = dbQuery {
        val insertOpinion = Feedbacks.insert {
            it[this.userId] = userId
            it[this.text] = text
            it[this.createdAt] = LocalDateTime.now()
        }
        insertOpinion.resultedValues?.singleOrNull() != null
    }
}