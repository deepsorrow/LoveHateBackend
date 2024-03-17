package com.kropotov.lovehatebackend.db.dao.users

import com.kropotov.lovehatebackend.db.models.*

interface UsersDAOFacade {
    suspend fun getUser(id: Int): User?

    suspend fun addUser(username: String, password: String, email: String, role: UserRole): User?

    suspend fun deleteUser(id: Int): Boolean
}