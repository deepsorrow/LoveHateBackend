package com.kropotov.lovehatebackend

//import com.auth0.jwt.JWT
//import com.auth0.jwt.algorithms.Algorithm
import at.favre.lib.crypto.bcrypt.BCrypt
import com.apurebase.kgraphql.GraphQL
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.kropotov.lovehatebackend.db.dao.DatabaseSingleton
import com.kropotov.lovehatebackend.db.dao.users.UsersDAOFacadeImpl
import com.kropotov.lovehatebackend.routes.configureGraphQlRouting
import com.kropotov.lovehatebackend.routes.configureSerialization
import com.kropotov.lovehatebackend.utilities.createJwtToken
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    DatabaseSingleton.init(environment.config)

    val usersDao = UsersDAOFacadeImpl()

    val jwtRealm = environment.config.property("jwt.realm").getString()
    val secret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()
    install(Authentication) {
        jwt {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )

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
            authenticate(optional = true, build = it)
        }

        context { call ->
            call.authentication.principal<JWTPrincipal>()?.let {
                +it
            }
        }

        schema {
            configureGraphQlRouting()
        }

        configureSerialization()
    }


    install(DoubleReceive)
    routing {

        post("/login") {
            val credentials = call.receive<UserPasswordCredential>()
            val user = usersDao.getUser(credentials.username)
            
            val isPasswordInvalid = if (user == null) {
                true
            } else {
                !BCrypt.verifyer().verify(credentials.password.toCharArray(), user.passwordHash).verified
            }
            
            if (user == null || isPasswordInvalid) {
                call.respond(HttpStatusCode.Unauthorized)
            } else {
                usersDao.updateLastLoginAt(user.id)

                val jwtToken = createJwtToken(audience, issuer, secret, user.id)
                call.respond(hashMapOf("token" to jwtToken))
            }
        }

        post("/register") {
            val credentials = call.receive<UserPasswordCredential>()
            if (usersDao.getUser(credentials.username) != null) {
                call.respond(HttpStatusCode.Conflict, "This username already exist.")
                return@post
            }
            val user = credentials.run {
                val passwordHash = BCrypt.withDefaults().hashToString(8, password.toCharArray())
                usersDao.addUser(username, password, passwordHash)
            }

            if (user == null) {
                call.respond(HttpStatusCode.InternalServerError)
            } else {
                val jwtToken = createJwtToken(audience, issuer, secret, user.id)
                call.respond(HttpStatusCode.OK, hashMapOf("token" to jwtToken))
            }
        }

        get("/isUsernameOccupied") {
            val username = call.request.queryParameters["username"].orEmpty()
            val isOccupied = usersDao.getUser(username) != null
            call.respond(isOccupied)
        }
    }
}
