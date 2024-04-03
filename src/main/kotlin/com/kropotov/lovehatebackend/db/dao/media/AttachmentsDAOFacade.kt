package com.kropotov.lovehatebackend.db.dao.media

import com.kropotov.lovehatebackend.db.models.Attachment

interface AttachmentsDAOFacade {

    suspend fun getFullSizeMediaUrl(id: Int): String?

    suspend fun addMedia(opinionId: Int, thumbnailPath: String, srcPath: String): Attachment?

    suspend fun deleteMedia(id: Int): Boolean
}