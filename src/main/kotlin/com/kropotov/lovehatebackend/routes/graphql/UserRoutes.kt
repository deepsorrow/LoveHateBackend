package com.kropotov.lovehatebackend.routes.graphql

import com.apurebase.kgraphql.Context
import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import com.kropotov.lovehatebackend.db.dao.users.UsersDAOFacade
import com.kropotov.lovehatebackend.db.models.UserScoreTitle
import com.kropotov.lovehatebackend.db.models.UserStatistics
import com.kropotov.lovehatebackend.utilities.getUserId
import org.kodein.di.DI
import org.kodein.di.instance

enum class UsersListType {
    MOST_ACTIVE,
    MOST_TENDERHEARTED,
    MOST_MANY_SIDED,
    MOST_SPITEFUL,
    MOST_OBSESSED,
}

data class UsersListResponse(
    val totalPages: Int,
    val results: List<UserStatistics>
)

fun SchemaBuilder.userRoutes(kodein: DI) {
    val usersDao by kodein.instance<UsersDAOFacade>()

    type<UserStatistics> {
        description = "Topic count, opinion count, love index, etc. per user"
    }

    enum<UsersListType> {
        description = "Sort type to get list of users"
    }

    enum<UserScoreTitle> {
        description = "User's score title determined by user's score"
    }

    type<UsersListResponse> {
        description = "List of users with total pages for pagination"
    }

    query("user") {
        description = "Returns current user's statistics"
        resolver { context: Context ->
            usersDao.getUserStatistics(context.getUserId())
        }
    }

    query("users") {
        description = "Returns users sorted by [listType]"
        resolver { onlyFirst: Boolean, listType: UsersListType, page: Int ->
            val pageCount = usersDao.getUsersPageCount()
            val results = when (listType) {
                UsersListType.MOST_TENDERHEARTED -> usersDao.getMostTenderheartedUsers(onlyFirst, page)
                UsersListType.MOST_MANY_SIDED -> usersDao.getMostManySidedUsers(onlyFirst, page)
                UsersListType.MOST_SPITEFUL -> usersDao.getMostSpitefulUsers(onlyFirst, page)
                UsersListType.MOST_OBSESSED -> usersDao.getMostObsessedUsers(onlyFirst, page)
                else -> usersDao.getMostActiveUsers(onlyFirst, page)
            }
            UsersListResponse(
                totalPages = pageCount,
                results = results
            )
        }
    }
}