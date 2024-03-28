package com.kropotov.lovehatebackend.db.dao.topics

import com.kropotov.lovehatebackend.db.dao.DatabaseSingleton.dbQuery
import com.kropotov.lovehatebackend.db.models.*
import com.kropotov.lovehatebackend.utilities.Constants.BATCH_TOPIC_AMOUNT
import com.kropotov.lovehatebackend.utilities.StringSimilarity
import com.kropotov.lovehatebackend.utilities.executeAndMap
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.sql.ResultSet
import java.time.LocalDateTime
import kotlin.math.floor

class TopicsDAOFacadeImpl : TopicsDAOFacade {

    override suspend fun addNewTopic(title: String, userId: Int): Int? = dbQuery {
        val insertTopic = Topics.insert {
            it[Topics.title] = title
            it[Topics.userId] = userId
            it[createdAt] = LocalDateTime.now()
        }
        insertTopic.resultedValues?.singleOrNull()?.let { it[Topics.id] }
    }

    override suspend fun getTopicOverview(id: Int): TopicOverview? = dbQuery {
        selectTopicStats(
            filter = "WHERE t.id = $id"
        ).firstOrNull()
    }

    override suspend fun getTopicsPageCount(): Int {
        val topicsCount = dbQuery {
            Topics
                .selectAll()
                .count()
        }

        return floor(topicsCount.toDouble() / BATCH_TOPIC_AMOUNT.toDouble()).toInt()
    }

    override suspend fun editTopic(id: Int, title: String): Boolean = dbQuery {
        Topics.update({ Topics.id eq id }) {
            it[Topics.title] = title
        } > 0
    }

    override suspend fun findNewTopics(page: Int): List<TopicOverview> = dbQuery {
        selectTopicStats(
            orderBy = "created_at DESC",
            page = page
        )
    }

    override suspend fun findRecentTopics(page: Int): List<TopicOverview> = dbQuery {
        val extraFieldName = "lastOpinionDate"
        selectTopicStats(
            extraFieldExp = "MAX(o.created_at) as $extraFieldName,",
            extraFieldName = "$extraFieldName,",
            orderBy = "$extraFieldName DESC",
            page = page
        )
    }

    override suspend fun findMostPopularTopics(page: Int): List<TopicOverview> = dbQuery {
        selectTopicStats(
            orderBy = "opinions_count DESC",
            page = page
        )
    }

    override suspend fun findMostLovedTopics(page: Int): List<TopicOverview> = dbQuery {
        selectTopicStats(
            orderBy = "love_index DESC",
            page = page
        )
    }

    override suspend fun findMostControversialTopics(page: Int): List<TopicOverview> = dbQuery {
        selectTopicStats(
            orderBy = "50 - opinion_percent, opinion_count DESC",
            page = page
        )
    }

    override suspend fun findMostHatedTopics(page: Int): List<TopicOverview> = dbQuery {
        selectTopicStats(
            orderBy = "love_index",
            page = page
        )
    }

    override suspend fun findFavoriteTopics(userId: Int, page: Int): List<TopicOverview> = dbQuery {
        selectTopicStats(
            extraTable = "INNER JOIN Favorites f ON t.id = f.topic_id AND f.user_id = $userId",
            orderBy = "created_at DESC",
            page = page
        )
    }

    override suspend fun findUserTopics(userId: Int, page: Int): List<TopicOverview> = dbQuery {
        selectTopicStats(
            filter = "WHERE t.user_id = $userId",
            orderBy = "created_at DESC",
            page = page
        )
    }

    override suspend fun findSimilarTopics(topicId: Int): List<TopicOverview> = dbQuery {
        // TODO: Migrate to DBO SQL Function for better performance
        val topicTitle = getTopicOverview(topicId)?.title.orEmpty()
        selectTopicStats(
            filter = "WHERE t.id != $topicId"
        ).map {
            StringSimilarity.similarity(topicTitle, it.title) to it
        }
            .sortedBy { it.first }
            .take(3)
            .map { it.second }
    }

    override suspend fun deleteTopic(id: Int): Boolean = dbQuery {
        Topics.deleteWhere { Topics.id eq id } > 0
    }

    private fun selectTopicStats(
        extraTable: String = "",
        extraFieldExp: String = "",
        extraFieldName: String = "",
        orderBy: String = "opinions_count DESC",
        filter: String = "",
        page: Int = 0
    ) = ("" +
            " WITH TopicsOpinionsCount AS (" +
            " SELECT " +
            " t.id, t.title, t.user_id, t.created_at, $extraFieldExp " +
            " COUNT(o.topic_id) as opinions_count, " +
            " (SELECT COUNT(*) FROM Opinions o1 where o1.topic_id = t.id and o1.type = ${OpinionType.LOVE.ordinal}) as loveOpinionsCount," +
            " (SELECT COUNT(*) FROM Opinions o2 where o2.topic_id = t.id and o2.type = ${OpinionType.HATE.ordinal}) as hateOpinionsCount " +
            " FROM Topics t " +
            " LEFT JOIN Opinions o ON t.id = o.topic_id " +
            " GROUP BY t.id, t.user_id, t.title, t.created_at" +
            " ), TopicsOpinionsCount2 AS (" +
            " SELECT " +
            " t.id, t.title, t.user_id, t.created_at, t.opinions_count, $extraFieldName " +
            " CASE " +
            " WHEN loveOpinionsCount > hateOpinionsCount THEN ${OpinionType.LOVE.ordinal} " +
            " WHEN hateOpinionsCount > loveOpinionsCount THEN ${OpinionType.HATE.ordinal} " +
            " ELSE ${OpinionType.INDIFFERENCE.ordinal} " +
            " END as opinion_type," +
            " 100 - CASE " +
            " WHEN loveOpinionsCount > hateOpinionsCount THEN hateOpinionsCount * 100 / loveOpinionsCount " +
            " WHEN hateOpinionsCount > loveOpinionsCount THEN loveOpinionsCount * 100 / hateOpinionsCount " +
            " ELSE 50 " +
            " END as opinion_percent," +
            " loveOpinionsCount / COALESCE(NULLIF(hateOpinionsCount, 0), 1) as love_index" +
            " FROM TopicsOpinionsCount t " +
            " $extraTable " +
            " $filter " +
            " ORDER BY $orderBy " +
            " LIMIT $BATCH_TOPIC_AMOUNT OFFSET ${getOffset(page)})" +
            "" +
            " SELECT * FROM TopicsOpinionsCount2").executeAndMap { resultRowToTopicStats(it) }

    private fun resultRowToTopicStats(row: ResultSet) = TopicOverview(
        id = row.getInt("id"),
        title = row.getString("title"),
        opinionsCount = row.getInt("opinions_count"),
        opinionType = OpinionType.entries[row.getInt("opinion_type")],
        opinionPercent = row.getInt("opinion_percent"),
        userId = row.getInt("user_id"),
        createdAt = row.getString("created_at").split(".")[0],
    )

    private fun getOffset(page: Int) = page * BATCH_TOPIC_AMOUNT
}