package com.kropotov.lovehatebackend.db.dao.media

import com.kropotov.lovehatebackend.db.models.Media
import com.kropotov.lovehatebackend.db.models.MediaType
import com.kropotov.lovehatebackend.db.models.Opinion
import com.kropotov.lovehatebackend.db.models.OpinionType

interface MediaDAOFacade {
    suspend fun getMedia(id: Int): Media?

    suspend fun addMedia(topicId: Int, opinionId: Int, commentId: Int, srcPath: String, type: MediaType): Media?

    suspend fun deleteMedia(id: Int): Boolean
}