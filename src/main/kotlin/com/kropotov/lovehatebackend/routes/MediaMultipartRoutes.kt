package com.kropotov.lovehatebackend.routes

import com.kropotov.lovehatebackend.db.dao.media.AttachmentsDAOFacade
import com.kropotov.lovehatebackend.utilities.createThumbnail
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import java.io.File

const val ROOT_MEDIA = "media"
const val BLANK_AVATARS_ROOT = "$ROOT_MEDIA/blank_avatars/"

fun Application.mediaMultipartRoutes(kodein: DI) = routing {

    val attachmentsDao by kodein.instance<AttachmentsDAOFacade>()

    staticFiles(ROOT_MEDIA, File(ROOT_MEDIA))
    staticFiles("uploads", File("uploads"))

    get("/attachment/{id}") {
        val attachmentId = call.parameters["id"]?.toInt()
        if (attachmentId == null) {
            call.respond(HttpStatusCode.BadRequest)
        } else {
            val url = attachmentsDao.getFullSizeMediaUrl(attachmentId)!!
            call.respond(HttpStatusCode.OK, url)
        }
    }
    post("/uploadOpinionAttachments") {
        try {
            var fileName = ""
            var opinionId = 0

            val multipartData = call.receiveMultipart()
            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        val value = part.value.toInt()
                        when (part.name) {
                            "id" -> opinionId = value
                        }
                    }
                    is PartData.FileItem -> {
                        fileName = part.originalFileName as String
                        val fileBytes = part.streamProvider().readBytes()
                        val filePath = "uploads/opinions/$opinionId/$fileName"
                        val thumbnailPath = "uploads/opinions/thumbnails/$opinionId/$fileName"
                        File(filePath).run {
                            parentFile.mkdirs()

                            writeBytes(fileBytes)
                            createThumbnail(this, thumbnailPath)
                        }

                        attachmentsDao.addMedia(opinionId, thumbnailPath, filePath)
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