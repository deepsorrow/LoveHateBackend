package com.kropotov.lovehatebackend.db.dao.opinions

import com.kropotov.lovehatebackend.db.models.Opinion
import com.kropotov.lovehatebackend.db.models.OpinionListItem
import com.kropotov.lovehatebackend.db.models.OpinionType
import com.kropotov.lovehatebackend.db.models.Topic

interface OpinionsDAOFacade {
    suspend fun getOpinion(id: Int): Opinion?

    suspend fun getOpinionsPageCount(opinionType: OpinionType?): Int
    suspend fun getTopicAuthorOpinion(topicId: Int): Opinion
    suspend fun createOpinion(topicId: Int, userId: Int, text: String, type: OpinionType): Opinion?

    suspend fun editOpinion(id: Int, text: String): Boolean

    suspend fun findOpinionTypes(topicId: Int): List<Pair<OpinionType, Int>>

    suspend fun findLatestOpinions(topicId: Int?, opinionType: OpinionType?, page: Int): List<OpinionListItem>

    suspend fun findMostLikedOpinions(page: Int): List<Opinion>

    suspend fun findMostCondemnedOpinions(page: Int): List<Opinion>

    suspend fun findMostCommentedOpinions(page: Int): List<Opinion>

    suspend fun findFavoriteOpinions(userId: Int, page: Int)

    suspend fun findUserOpinions(userId: Int, page: Int): List<Topic>

    suspend fun deleteOpinion(id: Int): Boolean
}