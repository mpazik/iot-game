package dzida.server.app.store.database

import dzida.server.app.achievement.Achievement
import dzida.server.app.achievement.AchievementChange
import dzida.server.app.achievement.AchievementStore
import dzida.server.app.database.ConnectionProvider
import dzida.server.app.querydsl.QAchievementEvent.achievementEvent
import dzida.server.app.serialization.MessageSerializer
import dzida.server.app.store.http.loader.AchievementLoader
import dzida.server.app.store.http.loader.StaticDataLoader
import dzida.server.app.user.User
import dzida.server.core.basic.entity.Id
import dzida.server.core.basic.entity.Key

class AchievementStoreDb : AchievementStore {
    val connectionProvider: ConnectionProvider
    val eventSerializer: MessageSerializer = MessageSerializer.create(AchievementChange.classes)
    override val achievements: List<Achievement>

    constructor(connectionProvider: ConnectionProvider) {
        this.connectionProvider = connectionProvider
        this.achievements = AchievementLoader(StaticDataLoader()).loadAchievements()
    }

    override fun saveEvent(event: AchievementChange) {
        val eventType = eventSerializer.getMessageType(event)

        connectionProvider.withSqlFactory { sqlQueryFactory ->
            sqlQueryFactory.insert(achievementEvent!!)
                    .set(achievementEvent.userId, event.userId.intValue)
                    .set(achievementEvent.achievementKey, event.key.value)
                    .set(achievementEvent.type, eventType)
                    .execute()
        }
    }

    override fun getUserEvents(userId: Id<User>): Iterable<AchievementChange> {
        return connectionProvider.withSqlFactory<List<AchievementChange>> { sqlQueryFactory ->
            val fetch = sqlQueryFactory
                    .select(achievementEvent.userId, achievementEvent.achievementKey, achievementEvent.type)
                    .from(achievementEvent)
                    .where(achievementEvent.userId.eq(userId.intValue))
                    .fetch()
            fetch.map({ tuple ->
                val achievementType = tuple.get(achievementEvent.type)!!
                val achievementKey = Key<Achievement>(tuple.get(achievementEvent.achievementKey)!!)

                val messageClass = eventSerializer.getMessageClass(achievementType)
                checkNotNull(messageClass, { "Achievement type <$achievementType> does not exists. Wrong data in DB?" })

                @Suppress("UNCHECKED_CAST")
                val eventClass = (messageClass as Class<AchievementChange>)
                eventClass.getConstructor(Id::class.java, Key::class.java).newInstance(userId, achievementKey)
            })
        }
    }

}
