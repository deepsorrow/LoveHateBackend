package com.kropotov.lovehatebackend.routes.graphql

import com.apurebase.kgraphql.Context
import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import com.kropotov.lovehatebackend.db.dao.feedback.FeedbackDAOFacade
import com.kropotov.lovehatebackend.routes.models.UpdateResponse
import com.kropotov.lovehatebackend.utilities.getUserId
import org.kodein.di.DI
import org.kodein.di.instance

fun SchemaBuilder.feedbackRoutes(kodein: DI) {
    val feedbackDao by kodein.instance<FeedbackDAOFacade>()

    mutation("sendFeedback") {
        description = "Sends text feedback"
        resolver { context: Context, text: String ->

            val success = feedbackDao.insertFeedback(context.getUserId(), text)
            UpdateResponse(success)
        }
    }
}