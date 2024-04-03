package com.kropotov.lovehatebackend.utilities

import com.apurebase.kgraphql.Context
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.mortennobel.imagescaling.AdvancedResizeOp
import com.mortennobel.imagescaling.MultiStepRescaleOp
import io.ktor.server.auth.jwt.*
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.awt.image.BufferedImage
import java.io.File
import java.sql.ResultSet
import javax.imageio.ImageIO

fun Context.getUserId(): Int {
    val principal = get<JWTPrincipal>()
    return principal!!.payload.getClaim("id").asInt()
}

fun createJwtToken(audience: String, issuer: String, secret: String, userId: Int): String {
    return JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("id", userId)
        .sign(Algorithm.HMAC256(secret))
}

fun <T : Any> String.executeAndMap(
    parameters: Iterable<Pair<IColumnType, Any?>> = listOf(),
    transform: (ResultSet) -> T
): List<T> {
    val result = arrayListOf<T>()
    TransactionManager.current().connection.prepareStatement(this, false).run {
        fillParameters(parameters)
        executeQuery().let { resultSet ->
            while (resultSet.next()) {
                result += transform(resultSet)
            }
        }
    }
    return result
}

fun createThumbnail(sourceFile: File, destLocation: String): File {
    val destFile = File(destLocation)
    destFile.parentFile.mkdirs()

    val desiredWidth = 100
    val bufferedImage = sourceFile.inputStream().use { ImageIO.read(sourceFile) }
    val resizeImage = if (desiredWidth <= bufferedImage.width) {
        val nHeight = desiredWidth * bufferedImage.height / bufferedImage.width
        val rescale = MultiStepRescaleOp(desiredWidth, nHeight).apply {
            unsharpenMask = AdvancedResizeOp.UnsharpenMask.Soft
        }
        rescale.filter(bufferedImage, null)
    } else {
        bufferedImage
    }

    val formatNames = ImageIO.getWriterFormatNames().toList()
    val target = if (formatNames.contains(destFile.extension)) destFile else File(destFile.path + ".jpg")
    ImageIO.write(resizeImage, target.extension, target)

    bufferedImage.flush()
    return destFile
}
