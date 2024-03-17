package com.kropotov.lovehatebackend.utilities

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun String.mapToLocalDateTime(): LocalDateTime {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return LocalDateTime.parse(this, formatter)
}

fun LocalDateTime.mapToString(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return format(formatter)
}