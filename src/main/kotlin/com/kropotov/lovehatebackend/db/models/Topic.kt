package com.kropotov.lovehatebackend.db.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

data class Topic(
    val id: Int,
    val userId: Int,
    val title: String,
    val createdAt: String
)

data class TopicOverview(
    val id: Int,
    val userId: Int,
    val title: String,
    val opinionsCount: Int,
    val opinionType: OpinionType,
    val opinionPercent: Int,
    val createdAt: String
)

data class TopicPage(
    val id: Int,
    val title: String,
    val opinionsCount: Int,
    val opinionType: OpinionType,
    val percent: Int,
    val author: String,
    val authorOpinionType: OpinionType,
    val isFavorite: Boolean,
    val createdAt: String
)

object Topics : Table() {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id")
    val title = varchar("title", 128)
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}