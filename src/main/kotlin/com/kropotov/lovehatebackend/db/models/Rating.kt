package com.kropotov.lovehatebackend.db.models

enum class UsersRatingType {
    MOST_ACTIVE,
    MOST_INDIFFERENT,
    MOST_TENDERHEARTED,
    MOST_SPITEFUL,
    MOST_OBSESSED
}

enum class OpinionsRatingType {
    MOST_LOVED,
    MOST_COMMENTED,
    MOST_HATED
}

data class UserRating(
    val userId: Int,
    val username: String,
    val type: UsersRatingType,
    val score: Int
)

data class OpinionRating(
    val opinionId: Int,
    val username: String,
    val type: OpinionsRatingType,
    val score: Int
)