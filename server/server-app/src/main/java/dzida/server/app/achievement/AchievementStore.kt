package dzida.server.app.achievement

import dzida.server.app.user.User
import dzida.server.core.basic.entity.Id

interface AchievementStore {
    val achievements: List<Achievement>
    fun saveEvent(event: AchievementChange)

    fun getUserEvents(userId: Id<User>): Iterable<AchievementChange>
}