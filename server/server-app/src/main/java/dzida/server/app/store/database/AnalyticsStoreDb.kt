package dzida.server.app.store.database

import dzida.server.app.analytics.AnalyticsStore
import dzida.server.app.basic.entity.Id
import dzida.server.app.database.ConnectionProvider
import dzida.server.app.querydsl.QAnalyticsEvent.analyticsEvent
import dzida.server.app.querydsl.QAnalyticsEventData.analyticsEventData
import dzida.server.app.serialization.BasicJsonSerializer
import dzida.server.app.user.User

class AnalyticsStoreDb(private val connectionProvider: ConnectionProvider) : AnalyticsStore {
    override fun saveEvent(userId: Id<User>, type: String) {
        connectionProvider.withSqlFactory { sqlQueryFactory ->
            sqlQueryFactory.insert(analyticsEvent)
                    .set(analyticsEvent.userId, userId.intValue)
                    .set(analyticsEvent.type, type)
                    .execute()
        }
    }

    override fun saveEvent(userId: Id<User>, type: String, data: Any) {
        connectionProvider.withSqlFactory { sqlQueryFactory ->
            val messageData = BasicJsonSerializer.getSerializer().toJson(data)
            sqlQueryFactory.insert(analyticsEventData)
                    .set(analyticsEventData.userId, userId.intValue)
                    .set(analyticsEventData.type, type)
                    .set(analyticsEventData.data, messageData)
                    .execute()
        }
    }
}

