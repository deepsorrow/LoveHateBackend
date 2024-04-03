package com.kropotov.lovehatebackend.db.dao.media

import com.kropotov.lovehatebackend.db.dao.DatabaseSingleton.dbQuery
import com.kropotov.lovehatebackend.db.models.Attachment
import com.kropotov.lovehatebackend.db.models.AttachmentSource
import com.kropotov.lovehatebackend.db.models.Attachments
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class AttachmentsDAOFacadeImpl : AttachmentsDAOFacade {

    private fun resultRowToMedia(row: ResultRow) = Attachment(
        id = row[Attachments.id],
        opinionId = row[Attachments.opinionId],
        imageUrl = row[Attachments.srcPath],
        source = row[Attachments.attachmentSource]
    )
    override suspend fun getFullSizeMediaUrl(id: Int): String? = dbQuery {
        Attachments
            .selectAll()
            .where { Attachments.id eq id }
            .map { it[Attachments.srcPath] }
            .firstOrNull()
    }

    override suspend fun addMedia(
        opinionId: Int,
        thumbnailPath: String,
        srcPath: String
    ) = dbQuery {
        val insertOpinion = Attachments.insert {
            it[this.opinionId] = opinionId
            it[this.thumbnailPath] = thumbnailPath
            it[this.srcPath] = srcPath
            it[this.attachmentSource] = AttachmentSource.GALLERY
        }
        insertOpinion.resultedValues?.singleOrNull()?.let(::resultRowToMedia)
    }

    override suspend fun deleteMedia(id: Int): Boolean = dbQuery {
        Attachments.deleteWhere { Attachments.id eq id } > 0
    }
}