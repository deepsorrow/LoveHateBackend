package com.kropotov.lovehatebackend.db.dao.myrating

import com.kropotov.lovehatebackend.db.dao.DatabaseSingleton.dbQuery
import com.kropotov.lovehatebackend.db.models.*
import com.kropotov.lovehatebackend.utilities.Constants.MY_RATED_EVENTS_AMOUNT
import com.kropotov.lovehatebackend.utilities.executeAndMap
import com.kropotov.lovehatebackend.utilities.mapToString
import kotlin.math.floor

class MyRatingDAOFacadeImpl : MyRatingDAOFacade {

    override suspend fun getLastRatedEventsTotalPages(userId: Int) = dbQuery {
        val count = selectMyRatedEvents(userId, 0, Int.MAX_VALUE).count()
        floor(count.toDouble() / MY_RATED_EVENTS_AMOUNT.toDouble()).toInt()
    }

    override suspend fun getLastRatedEvents(userId: Int, page: Int) = dbQuery {
        selectMyRatedEvents(userId, page)
    }

    private fun selectMyRatedEvents(
        userId: Int,
        page: Int,
        limit: Int = MY_RATED_EVENTS_AMOUNT
    ) =
        ("" +
                " WITH MyDislikes AS (" +
                " SELECT -1 as points, ${SourceType.DISLIKE.ordinal} as sourceType, o.text as text, opR.date" +
                " FROM OpinionReactions opR" +
                "   LEFT JOIN Opinions o" +
                "       ON opR.opinion_id = o.id" +
                " WHERE opR.user_id = $userId AND opR.type = ${ReactionType.DISLIKE.ordinal}" +
                " ), " +
                " MyOpinions AS (" +
                " SELECT 4 as points, ${SourceType.OPINION.ordinal} as sourceType, o.text as text, o.created_at as date" +
                " FROM Opinions o" +
                " WHERE o.user_id = $userId" +
                " )," +
                " OpinionsMinCreatedDate AS ( " +
                " SELECT o.topic_id, MIN(o.created_at) as created_at " +
                " FROM Opinions o " +
                " GROUP BY o.topic_id " +
                " ),  " +
                " TopicsWithAuthorOpinion AS (" +
                " SELECT oMinDate.topic_id, MIN(o.created_at) as created_at " +
                " FROM Opinions o" +
                "   LEFT JOIN OpinionsMinCreatedDate oMinDate" +
                "       ON oMinDate.topic_id = o.topic_id AND oMinDate.created_at = o.created_at " +
                " WHERE o.user_id = $userId " +
                " GROUP BY oMinDate.topic_id" +
                " )," +
                " MyTopics AS (" +
                " SELECT 10 as points, ${SourceType.TOPIC.ordinal} as sourceType, t.title as text, tt.created_at as date" +
                " FROM Topics t" +
                "   INNER JOIN TopicsWithAuthorOpinion tt" +
                "       ON t.id = tt.topic_id" +
                " )" +
                "" +
                " SELECT points, sourceType, text, date" +
                " FROM (" +
                "   SELECT * FROM myTopics" +
                "   UNION" +
                "   SELECT * FROM myOpinions" +
                "   UNION" +
                "   SELECT * FROM myDislikes" +
                " )" +
                " LIMIT $limit OFFSET ${getOffset(page)}").executeAndMap { row ->
                    MyRatingEvent(
                        sourceType = SourceType.values()[row.getInt("sourceType")],
                        text = row.getString("text").orEmpty(),
                        points = row.getInt("points"),
                        date = row.getTimestamp("date").toLocalDateTime().mapToString()
                    )
                }

    private fun getOffset(page: Int) = page * MY_RATED_EVENTS_AMOUNT
}