package com.kropotov.lovehatebackend.db.models

import org.jetbrains.exposed.sql.Table

data class TopicOverview(
    val id: Int,
    val title: String,
    val thumbnailUrl: String,
    val opinionsCount: Int,
    val opinionType: OpinionType,
    val opinionPercent: Int
)

data class TopicPage(
    val id: Int,
    val title: String,
    val opinionsCount: Int,
    val opinionType: OpinionType,
    val percent: Int,
    val author: String,
    val authorOpinionType: OpinionType,
    val attachmentsUrls: List<String> = listOf(),
    val isFavorite: Boolean,
    val createdAt: String
)

object Topics : Table() {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 128)
    val disabled = bool("disabled")

    override val primaryKey = PrimaryKey(id)
}