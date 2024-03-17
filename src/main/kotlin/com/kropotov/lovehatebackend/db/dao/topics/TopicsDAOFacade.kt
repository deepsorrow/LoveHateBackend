package com.kropotov.lovehatebackend.db.dao.topics

import com.kropotov.lovehatebackend.db.models.OpinionType
import com.kropotov.lovehatebackend.db.models.Topic

interface TopicsDAOFacade {
    suspend fun getTopic(id: Int): Topic?

    suspend fun getTopicsPageCount(): Int
    suspend fun addNewTopic(title: String, opinionType: OpinionType, userId: Int): Topic?

    suspend fun editTopic(id: Int, title: String): Boolean

    suspend fun updateTopic(topic: Topic)

    suspend fun findNewTopics(page: Int): List<Topic>

    suspend fun findRecentTopics(page: Int): List<Topic>

    suspend fun findMostPopularTopics(page: Int): List<Topic>

    suspend fun findMostLovedTopics(page: Int): List<Topic>

    suspend fun findMostIndifferentTopics(page: Int): List<Topic>

    suspend fun findMostHatedTopics(page: Int): List<Topic>

    suspend fun findFavoriteTopics(userId: Int, page: Int): List<Topic>

    suspend fun findUserTopics(userId: Int, page: Int): List<Topic>

    suspend fun findSimilarTopics(topicId: Int): List<Topic>

    suspend fun deleteTopic(id: Int): Boolean
}