package com.kropotov.lovehatebackend.db.dao.users

import com.kropotov.lovehatebackend.db.dao.DatabaseSingleton.dbQuery
import com.kropotov.lovehatebackend.db.models.*
import com.kropotov.lovehatebackend.utilities.mapToString
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime

class UsersDAOFacadeImpl : UsersDAOFacade {
    override suspend fun getUser(id: Int): User? = dbQuery {
        Users
            .selectAll()
            .where { Users.id eq id }
            .map(::resultRowToUser)
            .singleOrNull()
    }

    override suspend fun getUser(username: String): User? = dbQuery {
        Users
            .selectAll()
            .where { Users.username eq username }
            .map(::resultRowToUser)
            .singleOrNull()
    }

    override suspend fun addUser(username: String, passwordHash: String, email: String?): User? = dbQuery {
        val insertOpinion = Users.insert {
            it[this.username] = username
            it[this.passwordHash] = passwordHash
            it[this.email] = email
            it[this.score] = 0
            it[this.role] = UserRole.USER
            it[this.createdAt] = LocalDateTime.now()
        }
        insertOpinion.resultedValues?.singleOrNull()?.let(::resultRowToUser)
    }

    override suspend fun addUser(
        username: String,
        password: String,
        passwordHash: String,
        email: String?,
    ): User? = dbQuery {
        val insertOpinion = Users.insert {
            it[this.username] = username
            it[this.password] = password
            it[this.passwordHash] = passwordHash
            it[this.passwordUpdatedAt] = null
            it[this.photoUrl] = null
            it[this.email] = email
            it[this.score] = 0
            it[this.role] = UserRole.USER
            it[this.disabled] = false
            it[this.lastLoginAt] = LocalDateTime.now()
            it[this.createdAt] = LocalDateTime.now()
        }
        insertOpinion.resultedValues?.singleOrNull()?.let(::resultRowToUser)
    }

    override suspend fun updateLastLoginAt(id: Int) {
        dbQuery {
            Users.update({ Users.id eq id }) {
                it[this.lastLoginAt] = LocalDateTime.now()
            }
        }
    }

    override suspend fun deleteUser(id: Int): Boolean = dbQuery {
        Users.deleteWhere { Users.id eq id } > 0
    }

    override suspend fun getMostActiveUsers(): List<User> {
        val topicsCount = Topics.id.count().alias("topicsCount")
        val opinionsCount = Opinions.id.count().alias("opinionsCount")
        val commentsCount = Comments.id.count().alias("commentsCount")
        Users
            .leftJoin(Opinions, { id }, { userId })
            .leftJoin(Topics, { Users.id }, { userId })
            .leftJoin(Comments, { Users.id }, { userId })
            .select(Users.username, opinionsCount, topicsCount, commentsCount)
            .groupBy(Users.id)
            .map { row ->
                UserStatisticsDetailed(
                    id = row[Users.id],
                    username = row[Users.username],
                    score = row[Users.score],
                    scoreTitle = UserScoreTitle.getTitle(row[Users.score]),
                    topicsCount = row[topicsCount],
                    opinionsCount = row[opinionsCount],
                    commentsCount = row[commentsCount],
                    percent = row[Users.createdAt].mapToString(),
                    type = row[]
                )
            }
    }

    override suspend fun getMostTenderheartedUsers(): List<User> {
        val count = Opinions.type.count().alias("opinionsCount")
        Users
            .leftJoin(Opinions, { id }, { userId })
            .slice(Users.id, User)
            .selectAll()
            .where { Opinions.type eq OpinionType.LOVE }
            .groupBy(Opinions.type)
    }

    private fun resultRowToUser(row: ResultRow) = User(
        id = row[Users.id],
        username = row[Users.username],
        password = row[Users.password],
        passwordHash = row[Users.passwordHash],
        email = row[Users.email],
        score = row[Users.score],
        role = row[Users.role],
        createdAt = row[Users.createdAt].mapToString()
    )

    /**
     * val id: Int,
     *     val username: String,
     *     val score: Int,
     *     val scoreTitle: UserRatingTitle,
     *     val topicsCount: Int,
     *     val opinionsCount: Int,
     *     val commentsCount: Int,
     *     val percent: Int,
     *     val type: OpinionType
     */
//    private fun resultRowToUserStatistics(row: ResultRow) =
}