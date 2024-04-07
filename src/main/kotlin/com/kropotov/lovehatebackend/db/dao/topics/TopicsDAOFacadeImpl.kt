package com.kropotov.lovehatebackend.db.dao.topics

import com.kropotov.lovehatebackend.db.dao.DatabaseSingleton.dbQuery
import com.kropotov.lovehatebackend.db.models.*
import com.kropotov.lovehatebackend.utilities.Constants.BATCH_TOPIC_AMOUNT
import com.kropotov.lovehatebackend.utilities.StringSimilarity
import com.kropotov.lovehatebackend.utilities.executeAndMap
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.sql.ResultSet
import kotlin.math.floor

class TopicsDAOFacadeImpl : TopicsDAOFacade {

    override suspend fun addNewTopic(title: String, userId: Int): Int? = dbQuery {
        val insertTopic = Topics.insert {
            it[Topics.title] = title
        }
        insertTopic.resultedValues?.singleOrNull()?.let { it[Topics.id] }
    }

    override suspend fun getTopicOverview(id: Int): TopicOverview? = dbQuery {
        selectTopicStats(
            filterEnd = "AND t.id = $id"
        ).firstOrNull()
    }

    override suspend fun getTopicsPageCount(userId: Int?, searchQuery: String?): Int = dbQuery {
        val filterUserId = if (userId != null) {
            "WHERE user_id = $userId"
        } else {
            ""
        }

        val topicsCount = ("" +
                " WITH OpinionsMinCreatedDate AS ( " +
                " SELECT o.topic_id, MIN(o.created_at) as created_at " +
                " FROM Opinions o " +
                " GROUP BY o.topic_id " +
                " ),  " +
                " TopicsWithAuthorOpinion AS ( " +
                " SELECT oMinDate.topic_id, MIN(o.user_id) as user_id, MIN(o.created_at) as created_at " +
                " FROM OpinionsMinCreatedDate oMinDate " +
                "    LEFT JOIN Opinions o " +
                "        ON oMinDate.topic_id = o.topic_id AND oMinDate.created_at = o.created_at" +
                " $filterUserId " +
                " GROUP BY oMinDate.topic_id)" +
                "" +
                " SELECT COUNT(*) " +
                " FROM TopicsWithAuthorOpinion t" +
                "   LEFT JOIN Topics tt " +
                "       ON t.topic_id = tt.id" +
                " WHERE tt.title iLIKE ?")
            .executeAndMap(listOf(Pair(VarCharColumnType(), "%$searchQuery%"))) { it.getInt("count") }
            .first()

        floor(topicsCount.toDouble() / BATCH_TOPIC_AMOUNT.toDouble()).toInt()
    }

    override suspend fun editTopic(id: Int, title: String): Boolean = dbQuery {
        Topics.update({ Topics.id eq id }) {
            it[Topics.title] = title
        } > 0
    }

    override suspend fun findNewTopics(searchQuery: String?, page: Int): List<TopicOverview> = dbQuery {
        selectTopicStats(
            orderBy = "t.created_at DESC",
            searchQuery = searchQuery.orEmpty(),
            page = page
        )
    }

    override suspend fun findRecentTopics(searchQuery: String?, page: Int): List<TopicOverview> = dbQuery {
        val extraFieldName = "lastOpinionDate"
        selectTopicStats(
            extraFieldExp = "MAX(o.created_at) as $extraFieldName,",
            extraFieldName = "$extraFieldName,",
            orderBy = "$extraFieldName DESC",
            searchQuery = searchQuery.orEmpty(),
            page = page
        )
    }

    override suspend fun findMostPopularTopics(searchQuery: String?, page: Int): List<TopicOverview> = dbQuery {
        selectTopicStats(
            orderBy = "opinions_count DESC",
            searchQuery = searchQuery.orEmpty(),
            page = page
        )
    }

    override suspend fun findMostLovedTopics(searchQuery: String?, page: Int): List<TopicOverview> = dbQuery {
        selectTopicStats(
            orderBy = "opinion_percent DESC, hate_opinions_count DESC",
            filterEnd = "AND opinion_type = ${OpinionType.LOVE.ordinal}",
            searchQuery = searchQuery.orEmpty(),
            page = page
        )
    }

    override suspend fun findMostControversialTopics(searchQuery: String?, page: Int): List<TopicOverview> = dbQuery {
        selectTopicStats(
            orderBy = "50 - opinion_percent, opinion_count DESC",
            searchQuery = searchQuery.orEmpty(),
            page = page
        )
    }

    override suspend fun findMostHatedTopics(searchQuery: String?, page: Int): List<TopicOverview> = dbQuery {
        selectTopicStats(
            orderBy = "opinion_percent DESC, hate_opinions_count DESC",
            filterEnd = "AND opinion_type = ${OpinionType.HATE.ordinal}",
            searchQuery = searchQuery.orEmpty(),
            page = page
        )
    }

    override suspend fun findFavoriteTopics(userId: Int, searchQuery: String?, page: Int): List<TopicOverview> = dbQuery {
        selectTopicStats(
            extraTable = "INNER JOIN Favorites f ON t.id = f.topic_id AND f.user_id = $userId",
            orderBy = "created_at DESC",
            searchQuery = searchQuery.orEmpty(),
            page = page
        )
    }

    override suspend fun findUserTopics(userId: Int, searchQuery: String?, page: Int): List<TopicOverview> = dbQuery {
        selectTopicStats(
            filterBegin = "WHERE user_id = $userId",
            orderBy = "created_at DESC",
            searchQuery = searchQuery.orEmpty(),
            page = page
        )
    }

    override suspend fun findSimilarTopics(topicId: Int): List<TopicOverview> = dbQuery {
        // TODO: Migrate to DBO SQL Function for better performance
        val topicTitle = getTopicOverview(topicId)?.title.orEmpty()
        selectTopicStats(
            filterEnd = "AND t.id != $topicId"
        ).map {
            StringSimilarity.similarity(topicTitle, it.title) to it
        }
            .sortedByDescending { it.first }
            .take(3)
            .map { it.second }
    }

    override suspend fun findTopicAttachments(topicId: Int): List<String> = dbQuery {
        Topics
            .leftJoin(Opinions, { id }, { Opinions.topicId })
            .leftJoin(Attachments, { Opinions.id }, { opinionId })
            .select(Attachments.thumbnailPath).where {
                Topics.id eq topicId
            }
            .orderBy((Opinions.createdAt to SortOrder.DESC), (Attachments.id to SortOrder.DESC))
            .mapNotNull {
                it[Attachments.thumbnailPath]
            }
    }

    override suspend fun deleteTopic(id: Int): Boolean = dbQuery {
        Topics.deleteWhere { Topics.id eq id } > 0
    }

    private fun selectTopicStats(
        extraTable: String = "",
        extraFieldExp: String = "",
        extraFieldName: String = "",
        orderBy: String = "opinions_count DESC",
        filterBegin: String = "",
        filterEnd: String = "",
        searchQuery: String = "%",
        page: Int = 0
    ) = ("" +
            " WITH OpinionsMinCreatedDate AS (" +
            " SELECT o.topic_id, MIN(o.created_at) as created_at" +
            " FROM Opinions o" +
            " GROUP BY o.topic_id" +
            " ), " +
            " TopicsWithAuthorOpinion AS (" +
            " SELECT oMinDate.topic_id, MIN(o.user_id) as user_id, MIN(o.created_at) as created_at" +
            " FROM OpinionsMinCreatedDate oMinDate" +
            "   LEFT JOIN Opinions o" +
            "       ON oMinDate.topic_id = o.topic_id AND oMinDate.created_at = o.created_at" +
            " $filterBegin" +
            " GROUP BY oMinDate.topic_id" +
            " ), " +
            " LoveStats AS (" +
            " SELECT o.topic_id, COUNT(o.id) as love_opinions_count" +
            " FROM Opinions o" +
            " WHERE o.type = ${OpinionType.LOVE.ordinal}" +
            " GROUP BY o.topic_id" +
            " )," +
            " HateStats AS (" +
            " SELECT o.topic_id, COUNT(o.id) as hate_opinions_count" +
            " FROM Opinions o" +
            " WHERE o.type = ${OpinionType.HATE.ordinal}" +
            " GROUP BY o.topic_id" +
            " )," +
            " TopicsOpinionsCount AS (" +
            " SELECT t2.id, t2.title, t.user_id, MIN(t.created_at) as created_at, $extraFieldExp " +
            " COUNT(o.topic_id) as opinions_count, l.love_opinions_count, h.hate_opinions_count " +
            "   FROM TopicsWithAuthorOpinion t " +
            "       LEFT JOIN Topics t2" +
            "           ON t.topic_id = t2.id" +
            "       LEFT JOIN Opinions o " +
            "           ON t.topic_id = o.topic_id" +
            "       LEFT JOIN LoveStats l" +
            "           ON t.topic_id = l.topic_id" +
            "       LEFT JOIN HateStats h" +
            "           ON t.topic_id = h.topic_id " +
            " GROUP BY t2.id, t2.title, t.user_id, l.love_opinions_count, h.hate_opinions_count" +
            " ), TopicsOpinionsCount2 AS (" +
            " SELECT " +
            " t.id, t.title, t.opinions_count, love_opinions_count, hate_opinions_count, t.created_at, $extraFieldName " +
            " CASE " +
            " WHEN love_opinions_count > hate_opinions_count THEN ${OpinionType.LOVE.ordinal} " +
            " WHEN hate_opinions_count > love_opinions_count THEN ${OpinionType.HATE.ordinal} " +
            " ELSE ${OpinionType.INDIFFERENCE.ordinal} " +
            " END as opinion_type," +
            " CAST(CASE" +
            "   WHEN love_opinions_count > hate_opinions_count THEN love_opinions_count / (opinions_count * 1.0)" +
            "   ELSE hate_opinions_count / (opinions_count * 1.0)" +
            " END * 100 as int) as opinion_percent" +
            "   FROM TopicsOpinionsCount t " +
            "   $extraTable " +
            " ORDER BY $orderBy " +
            " )," +
            " TopicLastAttachment AS (" +
            " SELECT t.id as topic_id, MAX(a.id) as last_attachment_id" +
            "   FROM Topics t" +
            "       LEFT JOIN Opinions o" +
            "           ON t.id = o.topic_id" +
            "       LEFT JOIN Attachments a" +
            "           ON o.id = a.opinion_id" +
            " GROUP BY t.id" +
            " ORDER BY MAX(o.created_at), MAX(a.id)" +
            " )" +
            " SELECT t.id, t.title, t.love_opinions_count, t.hate_opinions_count, t.opinions_count," +
            " $extraFieldName t.opinion_type, t.opinion_percent, a.thumbnail_path " +
            " FROM TopicsOpinionsCount2 t" +
            "   LEFT JOIN TopicLastAttachment la" +
            "       ON t.id = la.topic_id" +
            "   LEFT JOIN Attachments a" +
            "       ON la.last_attachment_id = a.id" +
            " WHERE t.title iLIKE ? $filterEnd" +
            " ORDER BY $orderBy" +
            " LIMIT $BATCH_TOPIC_AMOUNT OFFSET ${getOffset(page)}").executeAndMap(
                listOf(Pair(VarCharColumnType(), "%$searchQuery%")) // prevent SQL Injection
            ) { resultRowToTopicStats(it) }

    private fun resultRowToTopicStats(row: ResultSet) = TopicOverview(
        id = row.getInt("id"),
        title = row.getString("title"),
        opinionsCount = row.getInt("opinions_count"),
        opinionType = OpinionType.entries[row.getInt("opinion_type")],
        opinionPercent = row.getInt("opinion_percent"),
        thumbnailUrl = row.getString("thumbnail_path").orEmpty()
    )

    private fun getOffset(page: Int) = page * BATCH_TOPIC_AMOUNT
}