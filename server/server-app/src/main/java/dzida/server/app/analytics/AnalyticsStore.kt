package dzida.server.app.analytics

import dzida.server.app.user.User
import dzida.server.core.basic.entity.Id


interface AnalyticsStore {
    fun saveEvent(userId: Id<User>, type: String)

    fun saveEvent(userId: Id<User>, type: String, data: Any)
}