package com.kropotov.lovehatebackend.db.models

import org.jetbrains.exposed.sql.Table

enum class AttachmentSource {
    GALLERY,
    UNSPLASH,
    SEARCH
}

data class Attachment(
    val id: Int,
    val opinionId: Int?,
    val imageUrl: String,
    val source: AttachmentSource
)

object Attachments : Table() {
    val id = integer("id").autoIncrement()
    val opinionId = integer("opinion_id").nullable()
    val srcPath = varchar("src_path", 256)
    val thumbnailPath = varchar("thumbnail_path", 256)
    val attachmentSource = enumeration<AttachmentSource>("source")

    override val primaryKey = PrimaryKey(id)
}