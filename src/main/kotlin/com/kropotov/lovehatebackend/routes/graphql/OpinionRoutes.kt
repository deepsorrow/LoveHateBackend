package com.kropotov.lovehatebackend.routes.graphql

import com.apurebase.kgraphql.Context
import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import com.kropotov.lovehatebackend.db.dao.opinions.OpinionsDAOFacade
import com.kropotov.lovehatebackend.db.models.*
import com.kropotov.lovehatebackend.utilities.getUserId
import org.kodein.di.DI
import org.kodein.di.instance

data class OpinionListResponse(
    val totalPages: Int,
    val results: List<OpinionListItem>
)

enum class OpinionsListType {
    ALL,
    BY_CURRENT_USER,
    BY_FAVORITES,
    MOST_LIKED,
    MOST_DISLIKED
}

fun SchemaBuilder.opinionRoutes(kodein: DI) {

    val opinionsDao by kodein.instance<OpinionsDAOFacade>()

    type<Opinion> {
        description = "User's opinion about specific topic"
    }

    type<OpinionListResponse> {
        description = "List of opinions with total pages for pagination"
    }

    enum<OpinionType> {
        description = "Love, Hate or Indifference expressed in Opinion"
    }

    enum<OpinionsListType> {
        description = "Filter type to get list of opinions"
    }

    query("opinions") {
        description = "Returns opinions sorted by date descending"
        resolver {
            context: Context, searchQuery: String?, topicId: Int?, opinionType: OpinionType?, listType: OpinionsListType?, onlyFirst: Boolean, page: Int ->

            val totalPages = opinionsDao.getOpinionsPageCount(
                userId = context.getUserId()
                    .takeIf { listType == OpinionsListType.BY_CURRENT_USER || listType == OpinionsListType.BY_FAVORITES },
                topicId = topicId,
                opinionType = opinionType,
                byFavorites = listType == OpinionsListType.BY_FAVORITES,
                searchQuery = searchQuery
            )
            val results = when (listType) {
                OpinionsListType.BY_CURRENT_USER -> opinionsDao.findUserOpinions(context.getUserId(), searchQuery, page)
                OpinionsListType.BY_FAVORITES -> opinionsDao.findFavoriteOpinions(context.getUserId(), searchQuery, page)
                OpinionsListType.MOST_LIKED -> opinionsDao.findMostLikedOpinions(context.getUserId(), onlyFirst, searchQuery, page)
                OpinionsListType.MOST_DISLIKED -> opinionsDao.findMostCondemnedOpinions(context.getUserId(), onlyFirst, searchQuery, page)
                else -> opinionsDao.findLatestOpinions(context.getUserId(), topicId, opinionType, searchQuery, page)
            }
            OpinionListResponse(
                totalPages = totalPages,
                results = results
            )
        }
    }

    mutation("publishOpinion") {
        description = "Publishes someone's valuable opinion"
        resolver { context: Context, topicId: Int, text: String, type: OpinionType ->

            opinionsDao.createOpinion(topicId, context.getUserId(), text, type)
        }
    }
}