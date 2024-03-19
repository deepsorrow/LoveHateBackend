package com.kropotov.lovehatebackend.routes.graphql

import com.apurebase.kgraphql.Context
import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import com.kropotov.lovehatebackend.db.dao.opinions.OpinionsDAOFacadeImpl
import com.kropotov.lovehatebackend.db.dao.topics.TopicsDAOFacadeImpl
import com.kropotov.lovehatebackend.db.dao.users.UsersDAOFacadeImpl
import com.kropotov.lovehatebackend.db.models.*
import com.kropotov.lovehatebackend.utilities.getUserId
import org.jetbrains.exposed.sql.transactions.transaction

fun SchemaBuilder.topicRoutes() {

    val topicsDao = TopicsDAOFacadeImpl()
    val usersDao = UsersDAOFacadeImpl()
    val opinionsDao = OpinionsDAOFacadeImpl()

    type<Topic> {
        description = "A topic to love&hate"
    }

    type<TopicPage> {
        description = "Detailed data about topic"
    }

    type<TopicsListResponse> {
        description = "List of topics with total pages for pagination"
    }

    enum<TopicsSortType> {
        description = "Topic sort type used to sort list `topics`"
    }

    query("topic") {
        description = "Returns topic by Id"
        resolver { id: Int ->
            topicsDao.getTopic(id)
        }
    }

    query("topicPage") {
        description = "Returns detailed data about topic for page"
        resolver { id: Int ->
            val topic = topicsDao.getTopic(id)!!
            val username = usersDao.getUser(topic.userId)?.username.orEmpty()
            val authorOpinion = opinionsDao.getTopicAuthorOpinion(id)
            TopicPage(
                id = id,
                title = topic.title,
                opinionsCount = topic.opinionsCount,
                opinionType = topic.opinionType,
                percent = topic.percent,
                author = username,
                authorOpinionType = authorOpinion.type,
                isFavorite = false,
                createdAt = topic.createdAt
            )
        }
    }

    query("topics") {
        description = "Returns all topics, sorted by [sortType]"
        resolver { context: Context, sortType: TopicsSortType?, page: Int ->
            val pageCount = topicsDao.getTopicsPageCount()
            val results = when (sortType) {
                TopicsSortType.RECENT -> topicsDao.findRecentTopics(page)
                TopicsSortType.NEW -> topicsDao.findNewTopics(page)
                TopicsSortType.POPULAR -> topicsDao.findMostPopularTopics(page)
                TopicsSortType.MOST_LOVED -> topicsDao.findMostLovedTopics(page)
                TopicsSortType.MOST_HATED -> topicsDao.findMostHatedTopics(page)
                TopicsSortType.FAVORITES -> topicsDao.findFavoriteTopics(context.getUserId(), page)
                TopicsSortType.BY_USER_ID -> topicsDao.findUserTopics(context.getUserId(), page)
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
        resolver { context: Context, title: String, opinionType: OpinionType, comment: String ->

            val userId = context.getUserId()
            val topic = topicsDao.addNewTopic(title, opinionType, userId)!!
            opinionsDao.createOpinion(topic.id, userId, comment, opinionType)
            topic
        }
    }

    mutation("editTopic") {
        description = "Edits topic text found by id"
        resolver { id: Int, text: String ->

            if (topicsDao.editTopic(id, text))
                topicsDao.getTopic(id)
            else
                throw throw UnknownError("Topic has not been updated")
        }
    }
}