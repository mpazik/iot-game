package dzida.server.app.achievement

import dzida.server.core.basic.entity.Key


data class Achievement(val key: Key<Achievement>, val unlock: AchievementUnlock)

data class AchievementUnlock(val eventSource: String, val eventName: String, val steps: Int = 1)