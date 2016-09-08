package dzida.server.app.achievement

import dzida.server.app.user.User
import dzida.server.core.basic.entity.Id
import dzida.server.core.basic.entity.Key

class UserAchievementState {
    private val usersAchievementsUnlocked: MutableMap<Id<User>, MutableMap<Key<Achievement>, Boolean>> = hashMapOf()
    private val usersAchievementsProgress: MutableMap<Id<User>, MutableMap<Key<Achievement>, Int>> = hashMapOf()

    fun isAchievementUnlocked(achievementKey: Key<Achievement>, userId: Id<User>): Boolean {
        return usersAchievementsUnlocked[userId]?.get(achievementKey) ?: false
    }

    fun getAchievementProgress(achievementKey: Key<Achievement>, userId: Id<User>): Int {
        return usersAchievementsProgress[userId]?.get(achievementKey) ?: 0
    }

    fun update(change: AchievementChange) {
        // if user is not logged into achievement server do not update they state
        val userAchievementsProgress = usersAchievementsProgress[change.userId] ?: return
        val userAchievementsUnlocked = usersAchievementsUnlocked[change.userId] ?: return

        when (change) {
            is AchievementChange.AchievementProgressed -> {
                val achievementProgress = userAchievementsProgress[change.key]
                if (achievementProgress == null) {
                    userAchievementsProgress[change.key] = 1
                } else {
                    userAchievementsProgress[change.key] = achievementProgress + 1
                }
            }
            is AchievementChange.AchievementUnlocked -> {
                userAchievementsUnlocked[change.key] = true
                if (userAchievementsProgress.containsKey(change.key)) {
                    userAchievementsProgress.remove(change.key)
                }
            }
        }
    }

    fun registerUser(userId: Id<User>) {
        usersAchievementsUnlocked[userId] = hashMapOf()
        usersAchievementsProgress[userId] = hashMapOf()
    }

    fun cleanUser(userId: Id<User>) {
        usersAchievementsUnlocked.remove(userId)
        usersAchievementsProgress.remove(userId)
    }

}