package com.kropotov.lovehatebackend.utilities

import com.apurebase.kgraphql.Context
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.jwt.*

fun Context.getUserId(): Int {
    val principal = get<JWTPrincipal>()
    return principal!!.payload.getClaim("id").asInt()
}

fun createJwtToken(audience: String, issuer: String, secret: String, userId: Int): String {
    return JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("id", userId)
        .sign(Algorithm.HMAC256(secret))
}