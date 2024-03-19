package com.kropotov.lovehatebackend.routes

import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import com.kropotov.lovehatebackend.routes.graphql.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

fun SchemaBuilder.configureGraphQlRouting() {
    topicRoutes()
    opinionRoutes()
    reactionRoutes()
    favoriteRoutes()
}

fun Application.configureRestRouting() {
    mediaMultipartRoutes()
    authRoutes()
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}
