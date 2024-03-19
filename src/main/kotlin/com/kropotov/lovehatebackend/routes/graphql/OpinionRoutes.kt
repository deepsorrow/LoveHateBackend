package com.kropotov.lovehatebackend.routes.graphql

import com.apurebase.kgraphql.Context
import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import com.kropotov.lovehatebackend.db.dao.opinions.OpinionsDAOFacadeImpl
import com.kropotov.lovehatebackend.db.dao.topics.TopicsDAOFacadeImpl
import com.kropotov.lovehatebackend.db.models.Opinion
import com.kropotov.lovehatebackend.db.models.OpinionListResponse
import com.kropotov.lovehatebackend.db.models.OpinionType
import com.kropotov.lovehatebackend.utilities.getUserId

fun SchemaBuilder.opinionRoutes() {

    val topicsDao = TopicsDAOFacadeImpl()
    val opinionsDao = OpinionsDAOFacadeImpl()

    type<Opinion> {
        description = "User's opinion about specific topic"
    }

    type<OpinionListResponse> {
        description = "List of opinions with total pages for pagination"
    }

    enum<OpinionType> {
        description = "Love, Hate or Indifference expressed in Opinion"
    }

    query("latestOpinions") {
        description = "Returns topic by Id"
        resolver { topicId: Int?, opinionType: OpinionType?, page: Int ->

            val totalPages = opinionsDao.getOpinionsPageCount(opinionType)
            val results = opinionsDao.findLatestOpinions(topicId, opinionType, page)
            OpinionListResponse(
                totalPages = totalPages,
                results = results
            )
        }
    }

    mutation("publishOpinion") {
        description = "Publishes someone's valuable opinion"
        resolver { context: Context, topicId: Int, text: String, type: OpinionType ->

            val opinion = opinionsDao.createOpinion(topicId, context.getUserId(), text, type)

            val topic = topicsDao.getTopic(topicId)!!
            topic.opinionsCount += 1

            val opinionsTypesCount = opinionsDao.findOpinionTypes(topic.id)
            topic.opinionType = opinionsTypesCount[0].first

            // Indifference may be only when no other opinions exist.
            val loveCount = opinionsTypesCount.find { it.first == OpinionType.LOVE }?.second ?: 0
            val hateCount = opinionsTypesCount.find { it.first == OpinionType.HATE }?.second ?: 0

            val percent = 100 - if (loveCount > hateCount) {
                hateCount * 100 / loveCount
            } else if (loveCount < hateCount) {
                loveCount * 100 / hateCount
            } else {
                topic.opinionType = OpinionType.INDIFFERENCE
                50 // loveCount == hateCount
            }
            topic.percent = percent
            topic.loveIndex = (loveCount / if (hateCount != 0) hateCount else 1).toDouble()

            topicsDao.updateTopic(topic)
            opinion
        }
    }
}