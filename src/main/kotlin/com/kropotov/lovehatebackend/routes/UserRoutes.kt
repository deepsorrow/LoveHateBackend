package com.kropotov.lovehatebackend.routes

import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import com.kropotov.lovehatebackend.db.dao.users.UsersDAOFacadeImpl
import com.kropotov.lovehatebackend.db.models.User
import com.kropotov.lovehatebackend.db.models.UserRole

fun SchemaBuilder.userRoutes() {

    val usersDao = UsersDAOFacadeImpl()

    type<User> {
        description = "An app's user"
    }

    enum<UserRole> {
        description = "User's role"
    }

    mutation("addUser") {
        description = "Adds new user"

        resolver { username: String, password: String, email: String, role: UserRole ->
            usersDao.addUser(username, password, email, role)
        }
    }
}