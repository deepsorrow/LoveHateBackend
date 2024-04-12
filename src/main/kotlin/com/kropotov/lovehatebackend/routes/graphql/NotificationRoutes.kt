package com.kropotov.lovehatebackend.routes.graphql

import com.apurebase.kgraphql.Context
import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import com.kropotov.lovehatebackend.db.dao.reactions.OpinionReactionDAOFacade
import com.kropotov.lovehatebackend.db.models.NotificationReaction
import com.kropotov.lovehatebackend.utilities.getUserId
import org.kodein.di.DI
import org.kodein.di.instance

fun SchemaBuilder.notificationRoutes(kodein: DI) {

    val dao by kodein.instance<OpinionReactionDAOFacade>()

    type<NotificationReaction> {
        description = "Simplified notification model prepared to show"
    }

    query("notifications") {
        description = "Get unread notifications"
        resolver { context: Context ->
            dao.findUnreadNotifications(context.getUserId()).onEach { notification ->
                dao.markAsRead(notification.userIdWhoFired, notification.opinionId)
            }
        }
    }
}