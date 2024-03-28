package com.kropotov.lovehatebackend.routes.graphql

import com.apurebase.kgraphql.Context
import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import com.kropotov.lovehatebackend.db.dao.favorites.FavoritesDAOFacade
import com.kropotov.lovehatebackend.db.dao.opinions.OpinionsDAOFacade
import com.kropotov.lovehatebackend.db.dao.topics.TopicsDAOFacade
import com.kropotov.lovehatebackend.db.dao.users.UsersDAOFacade
import com.kropotov.lovehatebackend.db.models.*
import com.kropotov.lovehatebackend.utilities.getUserId
import org.kodein.di.DI
import org.kodein.di.instance

enum class TopicsListType {
    RECENT,
    NEW,
    POPULAR,
    MOST_LOVED,
    MOST_CONTROVERSIAL,
    MOST_HATED,
    FAVORITES,
    BY_CURRENT_USER
}

data class TopicsListResponse(
    val totalPages: Int,
    val results: List<TopicOverview>
)

fun SchemaBuilder.topicRoutes(kodein: DI) {

    val topicsDao by kodein.instance<TopicsDAOFacade>()
    val usersDao by kodein.instance<UsersDAOFacade>()
    val opinionsDao by kodein.instance<OpinionsDAOFacade>()
    val favoritesDao by kodein.instance<FavoritesDAOFacade>()

    type<TopicOverview> {
        description = "A topic to love&hate with opinion counts and love percent"
    }

    type<TopicPage> {
        description = "Detailed data about topic"
    }

    type<TopicsListResponse> {
        description = "List of topics with total pages for pagination"
    }

    enum<TopicsListType> {
        description = "Topic sort type used to sort list `topics`"
    }

    query("topic") {
        description = "Returns topic by Id"
        resolver { id: Int ->
            topicsDao.getTopicOverview(id)
        }
    }

    query("topicPage") {
        description = "Returns detailed data about topic for page"
        resolver { context: Context, id: Int ->
            val topic = topicsDao.getTopicOverview(id)!!
            val author = usersDao.getUser(topic.userId)?.username.orEmpty()
            val authorOpinion = opinionsDao.getTopicAuthorOpinion(id)
            val isFavorite = favoritesDao.getFavorite(context.getUserId(), id, null, null) != null
            TopicPage(
                id = id,
                title = topic.title,
                opinionsCount = topic.opinionsCount,
                opinionType = topic.opinionType,
                percent = topic.opinionPercent,
                author = author,
                authorOpinionType = authorOpinion.type,
                isFavorite = isFavorite,
                createdAt = topic.createdAt
            )
        }
    }

    query("topics") {
        description = "Returns all topics, sorted by [listType]"
        resolver { context: Context, listType: TopicsListType?, page: Int ->
            val pageCount = topicsDao.getTopicsPageCount()
            val results = when (listType) {
                TopicsListType.RECENT -> topicsDao.findRecentTopics(page)
                TopicsListType.NEW -> topicsDao.findNewTopics(page)
                TopicsListType.POPULAR -> topicsDao.findMostPopularTopics(page)
                TopicsListType.MOST_LOVED -> topicsDao.findMostLovedTopics(page)
                TopicsListType.MOST_CONTROVERSIAL -> topicsDao.findMostControversialTopics(page)
                TopicsListType.MOST_HATED -> topicsDao.findMostHatedTopics(page)
                TopicsListType.FAVORITES -> topicsDao.findFavoriteTopics(context.getUserId(), page)
                TopicsListType.BY_CURRENT_USER -> topicsDao.findUserTopics(context.getUserId(), page)
                else -> topicsDao.findRecentTopics(page)
            }
            TopicsListResponse(
                totalPages = pageCount,
                results = results
            )
        }
    }

    query("similarTopics") {
        description = "Returns similar topics by topic id"
        resolver { topicId: Int ->
            topicsDao.findSimilarTopics(topicId)
        }
    }

    mutation("addTopic") {
        description = "Adds new topic"
        resolver { context: Context, title: String, opinionType: OpinionType, opinionText: String ->

            val userId = context.getUserId()
            val topicId = topicsDao.addNewTopic(title, userId)!!
            opinionsDao.createOpinion(topicId, userId, opinionText, opinionType)
            Topic(topicId, userId, title, "")
        }
    }

    mutation("editTopic") {
        description = "Edits topic text found by id"
        resolver { id: Int, text: String ->

            if (topicsDao.editTopic(id, text))
                topicsDao.getTopicOverview(id)
            else
                throw throw UnknownError("Topic has not been updated")
        }
    }
}