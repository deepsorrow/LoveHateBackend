package com.kropotov.lovehatebackend.utilities

import com.mortennobel.imagescaling.AdvancedResizeOp
import com.mortennobel.imagescaling.MultiStepRescaleOp
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import javax.imageio.ImageIO

object ThumbnailFactory {

    private val rootLocation: Path by lazy { Paths.get("media") }
    private val formatNames = ImageIO.getWriterFormatNames().toList()

    fun resizeImage(imgLocation: String, width: Int): File {
        val originFile = this.rootLocation.resolve(imgLocation).toFile()
        val destFile = this.rootLocation.resolve("resized-$width-${originFile.name}").toFile()

        val bufferedImage: BufferedImage = originFile.inputStream().use { ImageIO.read(it) }
        val resizeImage = if (width <= bufferedImage.width) {
            val nHeight = width * bufferedImage.height / bufferedImage.width
            val rescale = MultiStepRescaleOp(width, nHeight).apply { unsharpenMask = AdvancedResizeOp.UnsharpenMask.Soft }
            rescale.filter(bufferedImage, null)
        } else {
            bufferedImage
        }

        val target = if (formatNames.contains(destFile.extension)) destFile else File(destFile.path + ".jpg")
        ImageIO.write(resizeImage, target.extension, target)

        bufferedImage.flush()
        return destFile
    }
}