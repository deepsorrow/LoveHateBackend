package com.kropotov.lovehatebackend.db.models

enum class SourceType {
    TOPIC,
    OPINION,
    DISLIKE
}

data class MyRatingEventsResponse(
    val totalPages: Int,
    val results: List<MyRatingEvent>
)

data class MyRatingEvent(
    val sourceType: SourceType,
    val text: String,
    val points: Int,
    val date: String
)