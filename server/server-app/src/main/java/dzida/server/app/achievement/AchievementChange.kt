package dzida.server.app.achievement

import com.google.common.collect.ImmutableSet
import dzida.server.app.timesync.TimeServiceImpl
import dzida.server.app.user.User
import dzida.server.core.basic.entity.Id
import dzida.server.core.basic.entity.Key
import java.time.Instant


interface AchievementChange {
    companion object {
        val classes: ImmutableSet<Class<*>> = ImmutableSet.of<Class<*>>(
                AchievementProgressed::class.java,
                AchievementUnlocked::class.java)
    }

    val userId: Id<User>
    val key: Key<Achievement>
    val createdAt: Instant

    data class AchievementProgressed(
            override val userId: Id<User>,
            override val key: Key<Achievement>,
            override val createdAt: Instant = TimeServiceImpl.getServerInstant()
    ) : AchievementChange

    data class AchievementUnlocked(
            override val userId: Id<User>,
            override val key: Key<Achievement>,
            override val createdAt: Instant = TimeServiceImpl.getServerInstant()
    ) : AchievementChange

}