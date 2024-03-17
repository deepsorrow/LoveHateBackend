package com.kropotov.lovehatebackend.routes

import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

fun SchemaBuilder.configureGraphQlRouting() {
    topicRoutes()
    opinionRoutes()
    userRoutes()
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}
