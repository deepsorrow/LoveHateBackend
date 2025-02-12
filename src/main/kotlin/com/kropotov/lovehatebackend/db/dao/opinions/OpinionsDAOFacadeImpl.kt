package com.kropotov.lovehatebackend.db.dao.opinions

import com.kropotov.lovehatebackend.db.dao.DatabaseSingleton.dbQuery
import com.kropotov.lovehatebackend.db.models.*
import com.kropotov.lovehatebackend.utilities.Constants.BATCH_OPINION_AMOUNT
import com.kropotov.lovehatebackend.utilities.executeAndMap
import com.kropotov.lovehatebackend.utilities.ilike
import com.kropotov.lovehatebackend.utilities.mapToString
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.sql.ResultSet
import java.time.LocalDateTime
import kotlin.math.floor

class OpinionsDAOFacadeImpl : OpinionsDAOFacade {

    override suspend fun getOpinion(id: Int): Opinion? = dbQuery {
        Opinions
            .selectAll()
            .where { Opinions.id eq id }
            .map(::resultRowToOpinion)
            .singleOrNull()
    }

    override suspend fun getOpinionsPageCount(
        userId: Int?,
        topicId: Int?,
        opinionType: OpinionType?,
        filterByFavorites: Boolean,
        searchQuery: String?
    ): Int {
        val opinionsCount = dbQuery {
            if (filterByFavorites) {
                Opinions
                    .innerJoin(Favorites, { id }, { opinionId })
                    .selectAll()
                    .where { Favorites.userId eq userId!! }
                    .count()
            } else {
                Opinions
                    .selectAll()
                    .where {
                        val filterUserId = if (userId != null) Opinions.userId eq userId else Op.TRUE
                        val filterTopicId = if (topicId != null) Opinions.topicId eq topicId else Op.TRUE
                        val filterType = if (opinionType != null) Opinions.type eq opinionType else Op.TRUE
                        val filterSearch = if (searchQuery != null) Opinions.text ilike "%$searchQuery%" else Op.TRUE
                        filterUserId and filterTopicId and filterType and filterSearch
                    }
                    .count()
            }
        }

        return floor(opinionsCount.toDouble() / BATCH_OPINION_AMOUNT.toDouble()).toInt()
    }

    override suspend fun getTopicAuthorOpinion(topicId: Int): Opinion = dbQuery {
        Opinions
            .selectAll()
            .where { Opinions.topicId eq topicId }
            .orderBy(Opinions.createdAt, SortOrder.ASC)
            .first()
            .run(::resultRowToOpinion)
    }

    override suspend fun createOpinion(topicId: Int, userId: Int, text: String, type: OpinionType): Opinion? = dbQuery {
        val insertOpinion = Opinions.insert {
            it[this.topicId] = topicId
            it[this.userId] = userId
            it[this.text] = text
            it[this.type] = type
            it[createdAt] = LocalDateTime.now()
        }
        insertOpinion.resultedValues?.singleOrNull()?.let(::resultRowToOpinion)
    }

    override suspend fun editOpinion(id: Int, text: String): Boolean = dbQuery {
        Opinions.update({ Opinions.id eq id }) {
            it[Opinions.text] = text
        } > 0
    }

    override suspend fun findOpinionTypes(topicId: Int): List<Pair<OpinionType, Int>> = dbQuery {
        val count = Opinions.type.count().alias("count")
        Opinions
            .select(Opinions.type, count)
            .where { Opinions.topicId eq topicId }
            .groupBy(Opinions.type)
            .orderBy(count, SortOrder.DESC)
            .map {
                it[Opinions.type] to it[count].toInt()
            }
    }

    override suspend fun findLatestOpinions(
        userId: Int,
        topicId: Int?,
        opinionType: OpinionType?,
        searchQuery: String?,
        page: Int
    ): List<OpinionListItem> =
        dbQuery {
            val filter = when {
                topicId != null && opinionType != null -> "WHERE t.id = $topicId and o.type = ${opinionType.ordinal}"
                topicId != null && opinionType == null -> "WHERE t.id = $topicId"
                topicId == null && opinionType != null -> "WHERE o.type = ${opinionType.ordinal}"
                else -> ""
            }
            selectOpinions(
                userId = userId,
                filter = filter,
                searchQuery = searchQuery.orEmpty(),
                filterByTopic = topicId != null,
                page = page
            )
        }

    override suspend fun findMostLikedOpinions(
        userId: Int,
        onlyFirst: Boolean,
        searchQuery: String?,
        page: Int
    ): List<OpinionListItem> = dbQuery {
        selectOpinions(
            userId = userId,
            orderBy = "ORDER BY like_count DESC, created_at DESC",
            searchQuery = searchQuery.orEmpty(),
            page = page,
            limit = getLimit(onlyFirst)
        )
    }

    override suspend fun findMostCondemnedOpinions(
        userId: Int,
        onlyFirst: Boolean,
        searchQuery: String?,
        page: Int
    ): List<OpinionListItem> = dbQuery {
        selectOpinions(
            userId = userId,
            orderBy = "ORDER BY dislike_count DESC, created_at DESC",
            searchQuery = searchQuery.orEmpty(),
            page = page,
            limit = getLimit(onlyFirst)
        )
    }

    override suspend fun findMostCommentedOpinions(userId: Int, searchQuery: String?, page: Int): List<OpinionListItem> {
        TODO("Comments feature not yet implemented")
    }

    override suspend fun findFavoriteOpinions(userId: Int, searchQuery: String?, page: Int) = dbQuery {
        selectOpinions(
            userId = userId,
            filter = "WHERE f.opinion_id IS NOT NULL",
            searchQuery = searchQuery.orEmpty(),
            page = page
        )
    }

    override suspend fun findUserOpinions(userId: Int, searchQuery: String?, page: Int): List<OpinionListItem> =
        dbQuery {
            selectOpinions(
                userId = userId,
                filter = "WHERE u.id = $userId",
                searchQuery = searchQuery.orEmpty(),
                page = page
            )
        }

    override suspend fun deleteOpinion(id: Int): Boolean = dbQuery {
        Opinions.deleteWhere { Opinions.id eq id } > 0
    }

    private fun resultRowToOpinion(row: ResultRow) = Opinion(
        id = row[Opinions.id],
        topicId = row[Opinions.topicId],
        userId = row[Opinions.userId],
        text = row[Opinions.text],
        type = row[Opinions.type],
        createdAt = row[Opinions.createdAt].mapToString(row[Opinions.id]),
    )

    private fun selectOpinions(
        userId: Int,
        filter: String = "",
        orderBy: String = "ORDER BY created_at DESC",
        limit: Int = BATCH_OPINION_AMOUNT,
        searchQuery: String = "",
        filterByTopic: Boolean = false,
        page: Int
    ): List<OpinionListItem> {
        // No need to search on topic title, if search already inside topic
        val searchFilter = when {
            filterByTopic && searchQuery.isNotEmpty() -> "WHERE t.text iLIKE ? OR t.username iLIKE ?"
            searchQuery.isNotEmpty() -> "WHERE t.text iLIKE ? OR t.title iLIKE ? OR t.username iLIKE ?"
            else -> ""
        }
        // Prepared statement prevents SQL Injection
        val params = if (searchQuery.isEmpty()) listOf() else buildList {
            add(Pair(VarCharColumnType(), "%$searchQuery%"))
            add(Pair(VarCharColumnType(), "%$searchQuery%"))
            if (!filterByTopic) {
                add(Pair(VarCharColumnType(), "%$searchQuery%"))
            }
        }
        val hideBlocked = "t.disabled = false and o.disabled = false"
        val prefilter = if (filter.isEmpty()) "WHERE $hideBlocked" else "$filter and $hideBlocked"

        return ("" +
                " WITH TempTable AS (" +
                " SELECT o.id, u.username, t.title, o.text, o.type, o.created_at, u.id as user_id, " +
                " CASE WHEN opR.type = ${ReactionType.LIKE.ordinal} THEN True Else False END as is_liked," +
                " CASE WHEN opR.type = ${ReactionType.DISLIKE.ordinal} THEN True Else False END as is_disliked," +
                " CASE WHEN f.opinion_id IS NULL THEN False ELSE True END as is_favorite " +
                "" +
                " FROM Opinions o " +
                "   INNER JOIN Topics t " +
                "       ON o.topic_id = t.id " +
                "   INNER JOIN Users u " +
                "       ON o.user_id = u.id " +
                "   LEFT JOIN Favorites f " +
                "       ON o.id = f.opinion_id and f.user_id = $userId " +
                "   LEFT JOIN OpinionReactions opR " +
                "       ON o.id = opR.opinion_id and opR.user_id = $userId" +
                " $prefilter " +
                " ), OpinionStats3 AS (" +
                " SELECT t.id, t.username, t.title, t.text, t.type, t.created_at," +
                " t.is_liked, t.is_disliked, t.is_favorite, " +
                " COUNT(CASE WHEN opR.type = ${ReactionType.LIKE.ordinal} THEN 1 END) as like_count, " +
                " COUNT(CASE WHEN opR.type = ${ReactionType.DISLIKE.ordinal} THEN 1 END) as dislike_count " +
                " FROM TempTable t " +
                "   LEFT JOIN OpinionReactions opR " +
                "       ON t.id = opR.opinion_id" +
                " GROUP BY (t.id, t.username, t.title, t.text, t.type, t.created_at, t.is_liked, t.is_disliked, t.is_favorite)" +
                " $orderBy)" +
                "" +
                " SELECT t.id, t.username, t.title, t.text, t.type, t.created_at, t.like_count, t.dislike_count," +
                " t.is_liked, t.is_disliked, t.is_favorite, STRING_AGG(a.thumbnail_path, ';') as attachment_thumbnail_urls," +
                " ROW_NUMBER() OVER($orderBy) as position" +
                " FROM OpinionStats3 t" +
                "   LEFT JOIN Attachments a " +
                "       ON t.id = a.opinion_id" +
                " $searchFilter" +
                " GROUP BY (t.id, t.username, t.title, t.text, t.type, t.created_at, t.like_count, t.dislike_count," +
                " t.is_liked, t.is_disliked, t.is_favorite)" +
                " LIMIT $limit OFFSET ${getOffset(page)}").executeAndMap(params) { mapToOpinionListItem(it) }
    }

    private fun mapToOpinionListItem(row: ResultSet) =
        OpinionListItem(
            id = row.getInt("id"),
            username = row.getString("username"),
            topicTitle = row.getString("title"),
            text = row.getString("text"),
            type = OpinionType.entries[row.getInt("type")],
            date = row.getTimestamp("created_at").toLocalDateTime().mapToString(row.getInt("id")),
            likeCount = row.getInt("like_count").toString(),
            dislikeCount = row.getInt("dislike_count").toString(),
            isFavorite = row.getBoolean("is_favorite"),
            isLiked = row.getBoolean("is_liked"),
            isDisliked = row.getBoolean("is_disliked"),
            position = row.getInt("position"),
            attachmentUrls = row.getString("attachment_thumbnail_urls")?.split(";") ?: listOf()
        )

    private fun getOffset(page: Int) = page * BATCH_OPINION_AMOUNT

    private fun getLimit(onlyFirst: Boolean) = if (onlyFirst) 1 else BATCH_OPINION_AMOUNT
}