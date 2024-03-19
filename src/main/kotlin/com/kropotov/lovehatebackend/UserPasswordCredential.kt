package com.kropotov.lovehatebackend

import kotlinx.serialization.Serializable

@Serializable
data class UserPasswordCredential(
    val username: String,
    val password: String
)