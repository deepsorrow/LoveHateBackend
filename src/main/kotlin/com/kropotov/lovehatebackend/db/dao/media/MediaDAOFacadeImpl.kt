package com.kropotov.lovehatebackend.db.dao.media

import com.kropotov.lovehatebackend.db.dao.DatabaseSingleton.dbQuery
import com.kropotov.lovehatebackend.db.models.Media
import com.kropotov.lovehatebackend.db.models.MediaType
import com.kropotov.lovehatebackend.db.models.Multimedia
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class MediaDAOFacadeImpl : MediaDAOFacade {

    private fun resultRowToMedia(row: ResultRow) = Media(
        id = row[Multimedia.id],
        topicId = row[Multimedia.topicId],
        opinionId = row[Multimedia.opinionId],
        commentId = row[Multimedia.commentId],
        srcPath = row[Multimedia.srcPath],
        source = row[Multimedia.mediaSource],
        type = row[Multimedia.type],
    )
    override suspend fun getMedia(id: Int): Media? = dbQuery {
        Multimedia
            .selectAll()
            .where { Multimedia.id eq id }
            .map(::resultRowToMedia)
            .firstOrNull()
    }

    override suspend fun addMedia(
        topicId: Int,
        opinionId: Int,
        commentId: Int,
        srcPath: String,
        type: MediaType
    ) = dbQuery {
        val insertOpinion = Multimedia.insert {
            it[this.topicId] = topicId
            it[this.opinionId] = opinionId
            it[this.commentId] = commentId
            it[this.srcPath] = srcPath
            it[this.type] = type
        }
        insertOpinion.resultedValues?.singleOrNull()?.let(::resultRowToMedia)
    }

    override suspend fun deleteMedia(id: Int): Boolean = dbQuery {
        Multimedia.deleteWhere { Multimedia.id eq id } > 0
    }
}