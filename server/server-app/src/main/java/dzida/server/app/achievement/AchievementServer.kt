package dzida.server.app.achievement

import com.google.common.collect.ImmutableSet
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import dzida.server.app.achievement.AchievementChange.AchievementProgressed
import dzida.server.app.achievement.AchievementChange.AchievementUnlocked
import dzida.server.app.friend.FriendServer
import dzida.server.app.instance.UserMessage
import dzida.server.app.leaderboard.Leaderboard
import dzida.server.app.map.descriptor.Survival
import dzida.server.app.protocol.json.JsonProtocol
import dzida.server.app.serialization.BasicJsonSerializer
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
    private val leaderboard: Leaderboard
    private val achievementMessageSerializer: Gson = BasicJsonSerializer.getSerializerBuilder()
            .addSerializationExclusionStrategy(object : ExclusionStrategy {
                override fun shouldSkipClass(clazz: Class<*>?): Boolean = false
                override fun shouldSkipField(field: FieldAttributes): Boolean = field.name == "userId"
            })
            .create()
    private val protocol: JsonProtocol = JsonProtocol.create(achievementMessageSerializer, ImmutableSet.of(), AchievementChange.classes)
    private val userAchievementState: UserAchievementState = UserAchievementState()
    private val userTokenVerifier: UserTokenVerifier = UserTokenVerifier()

    private val achievementsBySourceAndName: Map<String, Map<String, List<Achievement>>>
    private val userConnections: MutableMap<Id<User>, Connector<String>> = hashMapOf()

    constructor(achievementStore: AchievementStore, leaderboard: Leaderboard) {
        this.achievementStore = achievementStore
        this.leaderboard = leaderboard
        this.achievementsBySourceAndName = achievementStore.achievements
                .groupBy { it.unlock.eventSource }
                .mapValues { it.value.groupBy { it.unlock.eventName } }
    }

    fun processUserGameEvent(userEvent: UserMessage.UserGameEvent): Unit {
        processUserMessage(userEvent, "instance")
    }

    fun processUserCommand(userCommand: UserMessage.UserCommand): Unit {
        processUserMessage(userCommand, "instance-command")
    }

    fun processUserFriendship(userCommand: FriendServer.UserFriendShipMessage): Unit {
        processUserMessage(userCommand, "friends")
    }

    fun processVictorySurvival(survival: Survival): Unit {
        leaderboard.update()
        val achievements = achievementsBySourceAndName.getOrElse("hardcoded", { hashMapOf() })
        val achievementChanges: Iterable<AchievementChange> = survival.attendees.flatMap { userId ->
            achievements.flatMap({ entry ->
                val achievementKey = entry.value[0].key
                if (userAchievementState.isAchievementUnlocked(achievementKey, userId)) {
                    emptyList()
                } else {
                    when (entry.key) {
                        "WinFirstBattle" -> listOf(AchievementUnlocked(userId, achievementKey))
                        "WinBattleLevel4" ->
                            if (survival.difficultyLevel >= 4) {
                                listOf(AchievementUnlocked(userId, achievementKey))
                            } else {
                                emptyList()
                            }
                        "BeFirstInRanking" ->
                            if (leaderboard.getPlayerScore(userId).position == 1) {
                                listOf(AchievementUnlocked(userId, achievementKey))
                            } else {
                                emptyList()
                            }
                        else -> emptyList()
                    }
                }
            })
        }
        processAchievementEvents(achievementChanges)
    }

    private fun processUserMessage(userMessage: UserMessage<Any>, messageSource: String) {
        val eventName = Event.getMessageTypeFromClass(userMessage.message.javaClass)
        val achievementsRelatedWithMessage = achievementsBySourceAndName
                .getOrElse(messageSource, { hashMapOf() })
                .getOrElse(eventName, { emptyList() })
        val achievementChanges = achievementsRelatedWithMessage.flatMap { processAchievement(userMessage.userId, it) }
        processAchievementEvents(achievementChanges)
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
            override fun send(data: String) {
            }

            override fun close() {
                userAchievementState.cleanUser(userId)
                userConnections.remove(userId)
            }
        })

        return Result.ok()
    }

}