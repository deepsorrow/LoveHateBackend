package com.kropotov.lovehatebackend.db.dao.users

import com.kropotov.lovehatebackend.db.models.*

interface UsersDAOFacade {
    suspend fun getUser(id: Int): User?

    suspend fun getUser(username: String): User?

    suspend fun addUser(username: String, passwordHash: String, email: String? = null): User?

    suspend fun addUser(username: String, password: String, passwordHash: String, email: String? = null): User?

    suspend fun updateLastLoginAt(id: Int)

    suspend fun getMostActiveUsers(): List<User>

    suspend fun getMostIndifferentUsers(): List<User>

    suspend fun getMostTenderheartedUsers(): List<User>

    suspend fun getMostSpitefulUsers(): List<Users>

    suspend fun getMostObsessedUsers(): List<Users>

    suspend fun deleteUser(id: Int): Boolean
}