package com.kropotov.lovehatebackend.db.dao.topics

import com.kropotov.lovehatebackend.db.models.TopicOverview

interface TopicsDAOFacade {
    suspend fun getTopicOverview(id: Int): TopicOverview?

    suspend fun getTopicsPageCount(): Int

    suspend fun addNewTopic(title: String, userId: Int): Int?

    suspend fun editTopic(id: Int, title: String): Boolean

    suspend fun findNewTopics(searchQuery: String?, page: Int): List<TopicOverview>

    suspend fun findRecentTopics(searchQuery: String?, page: Int): List<TopicOverview>

    suspend fun findMostPopularTopics(searchQuery: String?, page: Int): List<TopicOverview>

    suspend fun findMostLovedTopics(searchQuery: String?, page: Int): List<TopicOverview>

    suspend fun findMostControversialTopics(searchQuery: String?, page: Int): List<TopicOverview>

    suspend fun findMostHatedTopics(searchQuery: String?, page: Int): List<TopicOverview>

    suspend fun findFavoriteTopics(userId: Int, searchQuery: String?, page: Int): List<TopicOverview>

    suspend fun findUserTopics(userId: Int, searchQuery: String?, page: Int): List<TopicOverview>

    suspend fun findSimilarTopics(topicId: Int): List<TopicOverview>

    suspend fun deleteTopic(id: Int): Boolean
}