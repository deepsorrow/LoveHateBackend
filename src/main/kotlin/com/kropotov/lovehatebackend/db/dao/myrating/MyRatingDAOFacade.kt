package com.kropotov.lovehatebackend.db.dao.myrating

import com.kropotov.lovehatebackend.db.models.MyRatingEvent

interface MyRatingDAOFacade {

    suspend fun getLastRatedEventsTotalPages(userId: Int): Int

    suspend fun getLastRatedEvents(userId: Int, page: Int): List<MyRatingEvent>
}