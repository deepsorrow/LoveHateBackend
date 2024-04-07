package com.kropotov.lovehatebackend.utilities

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun LocalDateTime.mapToString(): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
    val formatted = format(formatter)
    return if (formatted.endsWith("00:00:00")) {
        formatted.dropLast(9)
    } else {
        formatted
    }
}