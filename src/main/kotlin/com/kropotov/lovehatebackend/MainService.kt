package com.kropotov.lovehatebackend

import com.apurebase.kgraphql.GraphQL
import com.kropotov.lovehatebackend.routes.graphql.*
import com.kropotov.lovehatebackend.routes.mediaMultipartRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import org.kodein.di.DI

fun Application.setupMainService(kodein: DI) {

    mediaMultipartRoutes(kodein)
    install(GraphQL) {
        playground = true
        endpoint = "/api/v1/"

        wrap {
            authenticate(optional = true, build = it)
        }

        context { call ->
            call.authentication.principal<JWTPrincipal>()?.let {
                +it
            }
        }

        schema {
            topicRoutes(kodein)
            opinionRoutes(kodein)
            reactionRoutes(kodein)
            favoriteRoutes(kodein)
            feedbackRoutes(kodein)
            userRoutes(kodein)
            notificationRoutes(kodein)
            myRatingRoutes(kodein)
        }

        install(ContentNegotiation) {
            json()
        }
    }
}