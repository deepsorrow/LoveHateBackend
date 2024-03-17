package com.kropotov.lovehatebackend.db.dao.users

import com.kropotov.lovehatebackend.db.dao.DatabaseSingleton.dbQuery
import com.kropotov.lovehatebackend.db.models.*
import com.kropotov.lovehatebackend.utilities.mapToString
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime

class UsersDAOFacadeImpl : UsersDAOFacade {
    private fun resultRowToUser(row: ResultRow) = User(
        id = row[Users.id],
        username = row[Users.username],
        password = row[Users.password],
        email = row[Users.email],
        score = row[Users.score],
        role = row[Users.role],
        date = row[Users.date].mapToString()
    )

    override suspend fun getUser(id: Int): User? = dbQuery {
        Users
            .select { Users.id eq id }
            .map(::resultRowToUser)
            .singleOrNull()
    }

    override suspend fun addUser(username: String, password: String, email: String, role: UserRole): User? = dbQuery {
        val insertOpinion = Users.insert {
            it[this.username] = username
            it[this.password] = password
            it[this.email] = email
            it[this.score] = 0
            it[this.role] = role
            it[this.date] = LocalDateTime.now()
        }
        insertOpinion.resultedValues?.singleOrNull()?.let(::resultRowToUser)
    }

    override suspend fun deleteUser(id: Int): Boolean = dbQuery {
        Users.deleteWhere { Users.id eq id } > 0
    }

}