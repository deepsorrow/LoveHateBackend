package com.kropotov.lovehatebackend.routes.graphql

import com.apurebase.kgraphql.Context
import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import com.kropotov.lovehatebackend.db.dao.myrating.MyRatingDAOFacade
import com.kropotov.lovehatebackend.db.models.MyRatingEvent
import com.kropotov.lovehatebackend.db.models.MyRatingEventsResponse
import com.kropotov.lovehatebackend.db.models.SourceType
import com.kropotov.lovehatebackend.utilities.getUserId
import org.kodein.di.DI
import org.kodein.di.instance

fun SchemaBuilder.myRatingRoutes(kodein: DI) {

    val dao by kodein.instance<MyRatingDAOFacade>()

    enum<SourceType> {
        description = "My rated event's source type, such as topic, opinion or dislike"
    }

    type<MyRatingEvent> {
        description = "My activity event which resulted in rating change"
    }

    type<MyRatingEventsResponse> {
        description = "List of my rated events with total pages for pagination"
    }

    query("myRatedEvents") {
        resolver { context: Context, page: Int ->
            val totalPages = dao.getLastRatedEventsTotalPages(context.getUserId())
            val results = dao.getLastRatedEvents(context.getUserId(), page)

            MyRatingEventsResponse(
                totalPages = totalPages,
                results = results
            )
        }
    }
}