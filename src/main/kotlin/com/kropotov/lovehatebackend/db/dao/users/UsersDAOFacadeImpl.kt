package com.kropotov.lovehatebackend.db.dao.users

import com.kropotov.lovehatebackend.db.dao.DatabaseSingleton.dbQuery
import com.kropotov.lovehatebackend.db.models.*
import com.kropotov.lovehatebackend.utilities.Constants.BATCH_USERS_AMOUNT
import com.kropotov.lovehatebackend.utilities.executeAndMap
import com.kropotov.lovehatebackend.utilities.mapToString
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.sql.ResultSet
import java.time.LocalDateTime
import kotlin.math.floor

class UsersDAOFacadeImpl : UsersDAOFacade {
    override suspend fun getUser(id: Int): User? = dbQuery {
        Users
            .selectAll()
            .where { Users.id eq id }
            .map(::resultRowToUser)
            .singleOrNull()
    }

    override suspend fun getUserStatistics(id: Int): UserStatistics? = dbQuery {
        selectUserStatistics(
            limit = 1,
            filter = "WHERE t.id = $id",
            page = 0
        ).firstOrNull()
    }

    override suspend fun getUsersPageCount(): Int {
        val usersCount = dbQuery {
            Users
                .selectAll()
                .count()
        }

        return floor(usersCount.toDouble() / BATCH_USERS_AMOUNT.toDouble()).toInt()
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

    override suspend fun getMostActiveUsers(onlyFirst: Boolean, page: Int) = dbQuery {
        selectUserStatistics(
            limit = getLimit(onlyFirst),
            page = page
        )
    }

    override suspend fun getMostManySidedUsers(onlyFirst: Boolean, page: Int) = dbQuery {
        selectUserStatistics(
            orderBy = "ORDER BY 50 - opinion_percent DESC, score DESC",
            limit = getLimit(onlyFirst),
            page = page
        )
    }

    override suspend fun getMostTenderheartedUsers(onlyFirst: Boolean, page: Int) = dbQuery {
        selectUserStatistics(
            orderBy = "ORDER BY opinion_percent DESC, score DESC",
            limit = getLimit(onlyFirst),
            page = page
        )
    }

    override suspend fun getMostSpitefulUsers(onlyFirst: Boolean, page: Int) = dbQuery {
        selectUserStatistics(
            orderBy = "ORDER BY opinion_percent, score DESC",
            limit = getLimit(onlyFirst),
            page = page
        )
    }

    override suspend fun getMostObsessedUsers(onlyFirst: Boolean, page: Int) = dbQuery {
        selectUserStatistics(
            orderBy = "ORDER BY topics_count, score DESC",
            limit = getLimit(onlyFirst),
            page = page
        )
    }

    private fun resultRowToUser(row: ResultRow) = User(
        id = row[Users.id],
        username = row[Users.username],
        password = row[Users.password],
        passwordHash = row[Users.passwordHash],
        email = row[Users.email],
        role = row[Users.role],
        createdAt = row[Users.createdAt].mapToString()
    )
    
    private fun selectUserStatistics(
        limit: Int = BATCH_USERS_AMOUNT,
        filter: String = "",
        orderBy: String = "ORDER BY score DESC",
        page: Int = 0
    ) = ("" +
            " WITH TopicStats AS (" +
            " SELECT u.id, u.username, u.photo_url, COUNT(*) as topics_count " +
            " FROM Users as u " +
            "   LEFT JOIN Topics t " +
            "       ON u.id = t.user_id " +
            " GROUP BY u.id, u.username" +
            " ), " +
            " OpinionStats AS ( " +
            " SELECT u.id, COUNT(*) as opinions_count, " +
            " (SELECT COUNT(*) FROM Opinions o1 where o1.user_id = u.id and o1.type = ${OpinionType.LOVE.ordinal}) as loveOpinionsCount, " +
            " (SELECT COUNT(*) FROM Opinions o2 where o2.user_id = u.id and o2.type = ${OpinionType.HATE.ordinal}) as hateOpinionsCount " +
            " FROM Users as u " +
            "   LEFT JOIN Opinions o " +
            " ON u.id = o.user_id " +
            " GROUP BY u.id" +
            " )," +
            " OpinionStats2 AS (" +
            " SELECT *," +
            " CAST(CASE" +
            "   WHEN loveOpinionsCount > hateOpinionsCount THEN loveOpinionsCount / (opinions_count * 1.0)" +
            "   ELSE hateOpinionsCount / (opinions_count * 1.0)" +
            " END * 100 as int) as opinion_percent," +
            " CASE WHEN loveOpinionsCount > hateOpinionsCount THEN 0" +
            "      WHEN hateOpinionsCount > loveOpinionsCount THEN 2" +
            "      ELSE 1" +
            " END as opinion_type " +
            " FROM OpinionStats" +
            " )," +
            " OpinionStats3 AS (" +
            " SELECT t.id, t.username, t.topics_count, t.photo_url, o.opinions_count," +
            " o.opinion_percent, o.opinion_type, t.topics_count * 10 + o.opinions_count * 4 as score" +
            " FROM TopicStats t" +
            "   INNER JOIN OpinionStats2 o" +
            "     ON t.id = o.id" +
            " $filter $orderBy" +
            ")" +
            " SELECT *, ROW_NUMBER() OVER($orderBy) as position FROM OpinionStats3" +
            " LIMIT $limit OFFSET ${getOffset(page)}").executeAndMap { resultRowToUserStatistics(it) }
    
    private fun resultRowToUserStatistics(row: ResultSet) = UserStatistics(
        id = row.getInt("id"),
        username = row.getString("username"),
        score = row.getInt("score"),
        scoreTitle = UserScoreTitle.getTitle(row.getInt("score")),
        topicsCount = row.getInt("topics_count"),
        opinionsCount = row.getInt("opinions_count"),
        opinionPercent = row.getInt("opinion_percent"),
        type = OpinionType.entries[row.getInt("opinion_type")],
        photoUrl = row.getString("photo_url").orEmpty(),
        position = row.getInt("position")
    )

    private fun getOffset(page: Int) = page * BATCH_USERS_AMOUNT

    private fun getLimit(onlyFirst: Boolean) = if (onlyFirst) 1 else BATCH_USERS_AMOUNT
}