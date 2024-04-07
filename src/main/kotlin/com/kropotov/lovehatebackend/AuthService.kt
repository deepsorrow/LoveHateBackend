package com.kropotov.lovehatebackend

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.kropotov.lovehatebackend.db.dao.users.UsersDAOFacade
import com.kropotov.lovehatebackend.utilities.createJwtToken
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.instance

@Serializable
data class UserPasswordCredential(
    val username: String,
    val password: String
)

fun Application.setupAuthService(kodein: DI) {
    val usersDao by kodein.instance<UsersDAOFacade>()

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
                usersDao.addUser(username, passwordHash)
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