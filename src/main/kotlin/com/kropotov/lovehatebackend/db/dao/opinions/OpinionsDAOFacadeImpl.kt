package com.kropotov.lovehatebackend.db.dao.opinions

import com.kropotov.lovehatebackend.db.dao.DatabaseSingleton.dbQuery
import com.kropotov.lovehatebackend.db.models.*
import com.kropotov.lovehatebackend.utilities.Constants.BATCH_OPINION_AMOUNT
import com.kropotov.lovehatebackend.utilities.mapToString
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime
import kotlin.math.floor

class OpinionsDAOFacadeImpl : OpinionsDAOFacade {

    override suspend fun getOpinion(id: Int): Opinion? = dbQuery {
        Opinions
            .select { Opinions.id eq id }
            .map(::resultRowToOpinion)
            .singleOrNull()
    }

    override suspend fun getOpinionsPageCount(opinionType: OpinionType?): Int {
        val opinionsCount = dbQuery {
            Opinions
                .select {
                    if (opinionType != null) Opinions.type eq opinionType else Op.TRUE
                }
                .count()
        }

        return floor(opinionsCount.toDouble() / BATCH_OPINION_AMOUNT.toDouble()).toInt()
    }

    override suspend fun getTopicAuthorOpinion(topicId: Int): Opinion = dbQuery {
        Opinions
            .select { Opinions.topicId eq topicId }
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
            .slice(Opinions.type, count)
            .select { Opinions.topicId eq topicId }
            .groupBy(Opinions.type)
            .orderBy(count, SortOrder.DESC)
            .map {
                it[Opinions.type] to it[count].toInt()
            }
    }

    override suspend fun findLatestOpinions(topicId: Int?, opinionType: OpinionType?, page: Int): List<OpinionListItem> =
        dbQuery {
            val topicTitle = Topics.title.alias("topicTitle")
            val username = Users.username.alias("username")
            Opinions
                .leftJoin(Topics, { Opinions.topicId }, { id })
                .leftJoin(Users, { Opinions.userId }, { id })
                .select(Opinions.id, topicTitle, username, Opinions.text, Opinions.type, Opinions.createdAt)
                .where {
                    val filterByTopicId = if (topicId != null) Opinions.topicId eq topicId else Op.TRUE
                    val filterByOpinionType = if (opinionType != null) Opinions.type eq opinionType else Op.TRUE
                    filterByTopicId and filterByOpinionType
                }
                .orderBy(Opinions.createdAt, SortOrder.DESC)
                .limitByPage(page)
                .sortedBy { Opinions.createdAt }
                .map {
                    OpinionListItem(
                        id = it[Opinions.id],
                        username = it[username],
                        topicTitle = it.getOrNull(topicTitle).orEmpty(),
                        text = it[Opinions.text],
                        type = it[Opinions.type],
                        date = it[Opinions.createdAt].mapToString()
                    )
                }
        }

    override suspend fun findMostLikedOpinions(page: Int): List<Opinion> {
        TODO("Not yet implemented")
    }

    override suspend fun findMostCondemnedOpinions(page: Int): List<Opinion> {
        TODO("Not yet implemented")
    }

    override suspend fun findMostCommentedOpinions(page: Int): List<Opinion> {
        TODO("Not yet implemented")
    }

    override suspend fun findFavoriteOpinions(userId: Int, page: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun findUserOpinions(userId: Int, page: Int): List<Topic> {
        TODO("Not yet implemented")
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
        createdAt = row[Opinions.createdAt].mapToString(),
    )

    private fun AbstractQuery<*>.limitByPage(page: Int)
        = limit(BATCH_OPINION_AMOUNT, (page * BATCH_OPINION_AMOUNT).toLong())
}