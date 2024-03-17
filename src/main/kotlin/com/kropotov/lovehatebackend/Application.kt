package com.kropotov.lovehatebackend

import com.apurebase.kgraphql.GraphQL
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.kropotov.lovehatebackend.db.dao.DatabaseSingleton
import com.kropotov.lovehatebackend.db.models.User
import com.kropotov.lovehatebackend.routes.configureGraphQlRouting
import com.kropotov.lovehatebackend.routes.configureMediaMultipartRoutes
import com.kropotov.lovehatebackend.routes.configureSerialization
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.util.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    DatabaseSingleton.init(environment.config)

    val jwtRealm = environment.config.property("jwt.realm").getString()
    val secret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()
    install(Authentication) {
        jwt {
            realm = jwtRealm
            verifier(JWT
                .require(Algorithm.HMAC256(secret))
                .withAudience(audience)
                .withIssuer(issuer)
                .build())

            validate { credential ->
                if (credential.payload.getClaim("id").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    install(GraphQL) {
        playground = true
        endpoint = "/api/v1/"

        wrap {
            authenticate(optional = false, build = it)
        }

        context { call ->
            call.authentication.principal<UserIdPrincipal>()?.let {
                +it
            }
        }

        schema {
            configureGraphQlRouting()
        }
        configureSerialization()
    }

    configureMediaMultipartRoutes()


    routing {
        staticFiles("/media/", File("media"))

        post("/login") {
            val user = call.receive<User>()

            val token = JWT.create()
                .withAudience(audience)
                .withIssuer(issuer)
                .withClaim("id", user.id)
                .sign(Algorithm.HMAC256(secret))
            call.respond(hashMapOf("token" to token))
        }
    }
}
