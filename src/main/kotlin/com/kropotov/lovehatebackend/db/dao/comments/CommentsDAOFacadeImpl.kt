package com.kropotov.lovehatebackend.db.dao.comments

import com.kropotov.lovehatebackend.db.dao.DatabaseSingleton.dbQuery
import com.kropotov.lovehatebackend.db.models.Comment
import com.kropotov.lovehatebackend.db.models.Comments
import com.kropotov.lovehatebackend.utilities.mapToString
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime

class CommentsDAOFacadeImpl : CommentsDAOFacade {
    private fun resultRowToComment(row: ResultRow) = Comment(
        id = row[Comments.id],
        userId = row[Comments.userId],
        text = row[Comments.text],
        opinionId = row[Comments.opinionId],
        date = row[Comments.date].mapToString(),
    )

    override suspend fun getComment(id: Int): Comment? = dbQuery {
        Comments
            .select { Comments.id eq id }
            .map(::resultRowToComment)
            .singleOrNull()
    }

    override suspend fun addNewComment(opinionId: Int, userId: Int, text: String): Comment? = dbQuery {
        val insertComment = Comments.insert {
            it[Comments.text] = text
            it[Comments.userId] = userId
            it[Comments.opinionId] = opinionId
            it[date] = LocalDateTime.now()
        }
        insertComment.resultedValues?.singleOrNull()?.let(::resultRowToComment)
    }

    override suspend fun editComment(id: Int, title: String): Boolean = dbQuery {
        Comments.update({ Comments.id eq id }) {
            it[text] = title
        } > 0
    }

    override suspend fun findMostPopularComments(page: Long): List<Comment> {
        TODO("Not yet implemented")
    }

    override suspend fun findMostLovedComments(page: Long): List<Comment> {
        TODO("Not yet implemented")
    }

    override suspend fun findMostHatedComments(page: Long): List<Comment> {
        TODO("Not yet implemented")
    }

    override suspend fun findFavoriteComments(userId: Int, page: Long): List<Comment> {
        TODO("Not yet implemented")
    }

    override suspend fun findUserComments(userId: Int, page: Long): List<Comment> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteComment(id: Int): Boolean = dbQuery {
        Comments.deleteWhere { Comments.id eq id } > 0
    }
}