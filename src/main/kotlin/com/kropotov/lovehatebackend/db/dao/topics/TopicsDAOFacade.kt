package com.kropotov.lovehatebackend.db.dao.topics

import com.kropotov.lovehatebackend.db.models.TopicOverview

interface TopicsDAOFacade {
    suspend fun getTopicOverview(id: Int): TopicOverview?

    suspend fun getTopicsPageCount(): Int

    suspend fun addNewTopic(title: String, userId: Int): Int?

    suspend fun editTopic(id: Int, title: String): Boolean

    suspend fun findNewTopics(page: Int): List<TopicOverview>

    suspend fun findRecentTopics(page: Int): List<TopicOverview>

    suspend fun findMostPopularTopics(page: Int): List<TopicOverview>

    suspend fun findMostLovedTopics(page: Int): List<TopicOverview>

    suspend fun findMostControversialTopics(page: Int): List<TopicOverview>

    suspend fun findMostHatedTopics(page: Int): List<TopicOverview>

    suspend fun findFavoriteTopics(userId: Int, page: Int): List<TopicOverview>

    suspend fun findUserTopics(userId: Int, page: Int): List<TopicOverview>

    suspend fun findSimilarTopics(topicId: Int): List<TopicOverview>

    suspend fun deleteTopic(id: Int): Boolean
}