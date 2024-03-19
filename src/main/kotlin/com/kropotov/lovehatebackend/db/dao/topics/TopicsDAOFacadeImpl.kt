package com.kropotov.lovehatebackend.db.dao.topics

import com.kropotov.lovehatebackend.db.dao.DatabaseSingleton.dbQuery
import com.kropotov.lovehatebackend.db.models.*
import com.kropotov.lovehatebackend.utilities.Constants.BATCH_TOPIC_AMOUNT
import com.kropotov.lovehatebackend.utilities.StringSimilarity
import com.kropotov.lovehatebackend.utilities.mapToString
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.time.LocalDateTime
import kotlin.math.floor

class TopicsDAOFacadeImpl : TopicsDAOFacade {

    override suspend fun addNewTopic(title: String, opinionType: OpinionType, userId: Int): Topic? = dbQuery {
        val insertTopic = Topics.insert {
            it[Topics.title] = title
            it[Topics.userId] = userId
            it[createdAt] = LocalDateTime.now()
        }
        insertTopic.resultedValues?.singleOrNull()?.let(::resultRowToTopic)
    }

    override suspend fun getTopic(id: Int): Topic? = dbQuery {
        Topics
            .selectAll()
            .where { Topics.id eq id }
            .map(::resultRowToTopic)
            .singleOrNull()
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

    override suspend fun findNewTopics(page: Int): List<Topic> = dbQuery {
        Topics
            .selectAll()
            .orderBy(Topics.createdAt, SortOrder.DESC)
            .limitByPage(page)
            .map(::resultRowToTopic)
    }

    override suspend fun findRecentTopics(page: Int): List<Topic> = dbQuery {
        Topics
            .leftJoin(Opinions, { id }, { topicId })
            .slice(Topics.columns)
            .selectAll()
            .groupBy(*Topics.columns.toTypedArray())
            .orderBy(Opinions.createdAt.max(), SortOrder.DESC)
            .limitByPage(page)
            .map(::resultRowToTopic)
    }

    override suspend fun findMostPopularTopics(page: Int): List<Topic> = dbQuery {
        val opinionsCount = Opinions.topicId.count().alias("opinionsCount")
        Topics
            .leftJoin(Opinions, { id }, { topicId })
            .select(opinionsCount, *Topics.columns.toTypedArray())
            .groupBy(Topics.id)
            .orderBy(opinionsCount, SortOrder.DESC)
            .limitByPage(page)
            .map(::resultRowToTopic)
    }

    override suspend fun findMostLovedTopics(page: Int): List<Topic> = dbQuery {
        val opinionTypesCount = Opinions.type.count().alias("")
//        Topics
//            .leftJoin(Opinions, { id }, { topicId }, { Opinions.type neq OpinionType.INDIFFERENCE })
//            .select(Topics.id, Topics.title, Topics.userId, Topics.createdAt, Opinions.type, opinionTypesCount)
//            .groupBy(Topics.id, Topics.title, Topics.userId, Topics.createdAt, Opinions.type)
//            .limitByPage(page)
//            .map(::resultRowToTopic)
        Opinions
            .select(Opinions.type)
            .where(Opinions.type neq OpinionType.INDIFFERENCE)
            .groupBy(Opinions., Opinions.type)
            .limitByPage(page)
            .map(::resultRowToTopic)
    }

    override suspend fun findMostIndifferentTopics(page: Int): List<Topic> = dbQuery {
        Topics
            .selectAll()
            .where { Topics.opinionType eq OpinionType.INDIFFERENCE }
            .orderBy(Topics.opinionsCount, SortOrder.DESC)
            .limitByPage(page)
            .map(::resultRowToTopic)
    }

    override suspend fun findMostHatedTopics(page: Int): List<Topic> = dbQuery {
        Topics
            .selectAll()
            .orderBy(Topics.loveIndex, SortOrder.ASC)
            .orderBy(Topics.opinionsCount, SortOrder.DESC)
            .limitByPage(page)
            .map(::resultRowToTopic)
    }

    override suspend fun findFavoriteTopics(userId: Int, page: Int): List<Topic> = dbQuery {
        Favorites
            .leftJoin(Topics, { topicId }, { id })
            .selectAll()
            .orderBy(Favorites.date, SortOrder.DESC)
            .limitByPage(page)
            .map(::resultRowToTopic)
    }

    override suspend fun findUserTopics(userId: Int, page: Int): List<Topic> = dbQuery {
        Users
            .leftJoin(Topics, { id }, { Topics.userId })
            .selectAll()
            .orderBy(Topics.createdAt, SortOrder.DESC)
            .limitByPage(page)
            .map(::resultRowToTopic)
    }

    override suspend fun findSimilarTopics(topicId: Int): List<Topic> {
        val topicTitle = getTopic(topicId)?.title.orEmpty()
        val similarTopicIds = dbQuery {
            Topics
                .selectAll()
                .where { Topics.id neq topicId }
                .map {
                    val rowTopic = resultRowToTopic(it)
                    StringSimilarity.similarity(topicTitle, rowTopic.title) to rowTopic
                }
                .sortedBy { it.first }
                .take(3)
                .map { it.second.id }
        }

        return dbQuery {
            Topics
                .selectAll()
                .orderBy(Topics.opinionsCount, SortOrder.DESC)
                .filter { similarTopicIds.contains(it[Topics.id]) }
                .map(::resultRowToTopic)
        }
    }

    override suspend fun deleteTopic(id: Int): Boolean = dbQuery {
        Topics.deleteWhere { Topics.id eq id } > 0
    }

    private fun resultRowToTopic(row: ResultRow) = Topic(
        id = row[Topics.id],
        userId = row[Topics.userId],
        title = row[Topics.title],
        createdAt = row[Topics.createdAt].mapToString(),
        opinionsCount = row[Topics.opinionsCount],
        opinionType = row[Topics.opinionType],
        percent = row[Topics.percent],
        loveIndex = row[Topics.loveIndex]
    )

    private fun AbstractQuery<*>.limitByPage(page: Int) =
        limit(BATCH_TOPIC_AMOUNT, (page * BATCH_TOPIC_AMOUNT).toLong())

    class SubQueryExpression<T>(private val aliasQuery : QueryAlias) : Expression<T>() {
        override fun toQueryBuilder(queryBuilder: QueryBuilder) {
            aliasQuery.describe(TransactionManager.current(), queryBuilder)
        }
    }
}