package com.kropotov.lovehatebackend.db.dao.users

import com.kropotov.lovehatebackend.db.models.*

interface UsersDAOFacade {
    suspend fun getUser(id: Int): User?

    suspend fun getUserStatistics(id: Int): UserStatistics?

    suspend fun getUsersPageCount(): Int

    suspend fun getUser(username: String): User?

    suspend fun addUser(username: String, passwordHash: String, email: String? = null): User?

    suspend fun addUser(username: String, password: String, passwordHash: String, email: String? = null): User?

    suspend fun updateLastLoginAt(id: Int)

    suspend fun getMostActiveUsers(onlyFirst: Boolean, page: Int): List<UserStatistics>

    suspend fun getMostManySidedUsers(onlyFirst: Boolean, page: Int): List<UserStatistics>

    suspend fun getMostTenderheartedUsers(onlyFirst: Boolean, page: Int): List<UserStatistics>

    suspend fun getMostSpitefulUsers(onlyFirst: Boolean, page: Int): List<UserStatistics>

    suspend fun getMostObsessedUsers(onlyFirst: Boolean, page: Int): List<UserStatistics>

    suspend fun deleteUser(id: Int): Boolean
}