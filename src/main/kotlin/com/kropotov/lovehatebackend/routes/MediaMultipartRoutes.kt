package com.kropotov.lovehatebackend.routes

import com.kropotov.lovehatebackend.db.dao.media.MediaDAOFacadeImpl
import com.kropotov.lovehatebackend.db.models.MediaType
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Application.configureMediaMultipartRoutes() = routing {

    val dao = MediaDAOFacadeImpl()

    post("/upload-media") {
        try {
            var fileName = ""
            var topicId = 0
            var opinionId = 0
            var commentId = 0
            val multipartData = call.receiveMultipart()

            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        val value = part.value.toInt()
                        when (part.name) {
                            "topicId" -> topicId = value
                            "opinionId" -> opinionId = value
                            "commentId" -> commentId = value
                        }
                    }
                    is PartData.FileItem -> {
                        File("uploads").mkdirs()
                        fileName = part.originalFileName as String
                        val fileBytes = part.streamProvider().readBytes()
                        val filePath = "uploads/$fileName"
                        File(filePath).writeBytes(fileBytes)

                        val mediaType = if (filePath.endsWith(".gif")) {
                            MediaType.GIF
                        } else if (filePath.endsWith(".png") || filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {
                            MediaType.IMAGE
                        } else if (filePath.endsWith(".mp4")|| filePath.endsWith(".webm")) {
                            MediaType.VIDEO
                        } else {
                            MediaType.UNKNOWN
                        }
                        dao.addMedia(topicId, opinionId, commentId, filePath, mediaType)
                    }
                    else -> {}
                }
            }

            call.respond(HttpStatusCode.Created, fileName)
        } catch (ex: Exception) {
            ex.printStackTrace()
            call.respond(HttpStatusCode.BadRequest, ex.message ?: "")
        }
    }
}