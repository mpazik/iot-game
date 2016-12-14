package dzida.server.app.analytics

import dzida.server.app.basic.entity.Id
import dzida.server.app.user.User


interface AnalyticsStore {
    fun saveEvent(userId: Id<User>, type: String)

    fun saveEvent(userId: Id<User>, type: String, data: Any)
}