package dzida.server.app.achievement

import com.google.common.collect.ImmutableSet
import dzida.server.app.achievement.AchievementChange.AchievementProgressed
import dzida.server.app.achievement.AchievementChange.AchievementUnlocked
import dzida.server.app.instance.UserGameEvent
import dzida.server.app.protocol.json.JsonProtocol
import dzida.server.app.user.EncryptedLoginToken
import dzida.server.app.user.User
import dzida.server.app.user.UserTokenVerifier
import dzida.server.core.basic.Event
import dzida.server.core.basic.Result
import dzida.server.core.basic.connection.Connector
import dzida.server.core.basic.connection.ServerConnection
import dzida.server.core.basic.connection.VerifyingConnectionServer
import dzida.server.core.basic.entity.Id

class AchievementServer : VerifyingConnectionServer<String, String> {
    private val achievementStore: AchievementStore
    private val protocol: JsonProtocol = JsonProtocol.create(ImmutableSet.of(), AchievementChange.classes)
    private val userAchievementState: UserAchievementState = UserAchievementState()
    private val userTokenVerifier: UserTokenVerifier = UserTokenVerifier()

    private val achievementsByEventName: Map<String, List<Achievement>>
    private val userConnections: MutableMap<Id<User>, Connector<String>> = hashMapOf()

    constructor(achievementStore: AchievementStore) {
        this.achievementStore = achievementStore
        this.achievementsByEventName = achievementStore.achievements.groupBy { it.unlock.eventName }
    }

    fun processUserGameEvent(userEvent: UserGameEvent): Unit {
        val eventName = Event.getMessageTypeFromClass(userEvent.event.javaClass)
        val unlockedAchievements = achievementsByEventName.getOrElse(eventName, { emptyList() })
        val achievementEvents = unlockedAchievements.flatMap { processAchievement(userEvent.userId, it) }
        processAchievementEvents(achievementEvents)
    }

    private fun processAchievement(userId: Id<User>, achievement: Achievement): Iterable<AchievementChange> {
        val key = achievement.key
        val emptyList: Iterable<AchievementChange> = emptyList()
        if (userAchievementState.isAchievementUnlocked(key, userId)) {
            return emptyList // user already has achievementKey
        }
        if (achievement.unlock.steps <= 1) {
            return listOf(AchievementUnlocked(userId, key))
        }
        val lastProgress = userAchievementState.getAchievementProgress(key, userId)
        val currentProgress = lastProgress + 1

        if (achievement.unlock.steps == currentProgress) {
            return listOf(AchievementProgressed(userId, key), AchievementUnlocked(userId, key))
        } else {
            return listOf(AchievementProgressed(userId, key))
        }
    }

    private fun processAchievementEvents(events: Iterable<AchievementChange>) {
        events.forEach { userAchievementState.update(it) }
        events.forEach { achievementStore.saveEvent(it) }
        events.forEach { sendToUser(it) }
    }

    private fun sendToUser(event: AchievementChange) {
        val message = protocol.serializeMessage(event)
        if (message != null) {
            userConnections[event.userId]?.onMessage(message)
        }
    }

    override fun onConnection(connector: Connector<String>, userToken: String): Result {
        val loginToken = userTokenVerifier.verifyToken(EncryptedLoginToken(userToken))
        if (!loginToken.isPresent) {
            return Result.error("Login to is invalid")
        }

        val userId = loginToken.get().userId
        userConnections[userId] = connector
        userAchievementState.registerUser(userId)
        achievementStore.getUserEvents(userId).forEach {
            userAchievementState.update(it)
            sendToUser(it)
        }
        connector.onOpen(object : ServerConnection<String> {
            override fun send(message: String) {
            }

            override fun close() {
                userAchievementState.cleanUser(userId)
            }
        })

        return Result.ok()
    }

}