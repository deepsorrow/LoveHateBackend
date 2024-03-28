package com.kropotov.lovehatebackend

import com.kropotov.lovehatebackend.db.dao.DatabaseSingleton
import com.kropotov.lovehatebackend.db.dao.comments.CommentsDAOFacade
import com.kropotov.lovehatebackend.db.dao.comments.CommentsDAOFacadeImpl
import com.kropotov.lovehatebackend.db.dao.favorites.FavoritesDAOFacade
import com.kropotov.lovehatebackend.db.dao.favorites.FavoritesDAOFacadeImpl
import com.kropotov.lovehatebackend.db.dao.feedback.FeedbackDAOFacade
import com.kropotov.lovehatebackend.db.dao.feedback.FeedbackDAOFacadeImpl
import com.kropotov.lovehatebackend.db.dao.media.MediaDAOFacade
import com.kropotov.lovehatebackend.db.dao.media.MediaDAOFacadeImpl
import com.kropotov.lovehatebackend.db.dao.opinions.OpinionsDAOFacade
import com.kropotov.lovehatebackend.db.dao.opinions.OpinionsDAOFacadeImpl
import com.kropotov.lovehatebackend.db.dao.reactions.OpinionReactionDAOFacade
import com.kropotov.lovehatebackend.db.dao.reactions.OpinionReactionDAOFacadeImpl
import com.kropotov.lovehatebackend.db.dao.topics.TopicsDAOFacade
import com.kropotov.lovehatebackend.db.dao.topics.TopicsDAOFacadeImpl
import com.kropotov.lovehatebackend.db.dao.users.UsersDAOFacade
import com.kropotov.lovehatebackend.db.dao.users.UsersDAOFacadeImpl
import com.kropotov.lovehatebackend.routes.mediaMultipartRoutes
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    DatabaseSingleton.init(environment.config)

    val kodein = DI {
        bind<CommentsDAOFacade>() with singleton { CommentsDAOFacadeImpl() }
        bind<FavoritesDAOFacade>() with singleton { FavoritesDAOFacadeImpl() }
        bind<UsersDAOFacade>() with singleton { UsersDAOFacadeImpl() }
        bind<TopicsDAOFacade>() with singleton { TopicsDAOFacadeImpl() }
        bind<OpinionsDAOFacade>() with singleton { OpinionsDAOFacadeImpl() }
        bind<OpinionReactionDAOFacade>() with singleton { OpinionReactionDAOFacadeImpl() }
        bind<MediaDAOFacade>() with singleton { MediaDAOFacadeImpl() }
        bind<FeedbackDAOFacade>() with singleton { FeedbackDAOFacadeImpl() }
    }

    setupAuthService(kodein)
    setupMainService(kodein)
    mediaMultipartRoutes()
}
