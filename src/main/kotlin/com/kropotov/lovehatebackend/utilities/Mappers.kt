package com.kropotov.lovehatebackend.utilities

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random

fun LocalDateTime.mapToString(seed: Int? = null): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy 'в' HH:mm", Locale("RU", "RU"))
    val formatted = format(formatter)
    return if (formatted.endsWith("00:00") && seed != null) {
        // TODO: Cache random time in db
        val millisInDay = 24*60*60*1000L
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val randomTime = LocalTime.MIN.plusSeconds(Random(seed).nextLong(millisInDay)).format(timeFormatter).takeLast(9)
        formatted.replace("00:00", randomTime)
    } else {
        formatted
    }
}