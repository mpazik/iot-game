package dzida.server.app.friend

import com.google.common.collect.ImmutableSet
import dzida.server.app.friend.FriendServer.ClientCommand.AcceptRequest
import dzida.server.app.friend.FriendServer.ClientCommand.RequestForFriendShip
import dzida.server.app.friend.FriendServer.ServerMessage.FriendshipEstablished
import dzida.server.app.friend.FriendServer.ServerMessage.FriendshipRequest
import dzida.server.app.protocol.json.JsonProtocol
import dzida.server.app.user.EncryptedLoginToken
import dzida.server.app.user.User
import dzida.server.app.user.UserStore
import dzida.server.app.user.UserTokenVerifier
import dzida.server.core.basic.Result
import dzida.server.core.basic.connection.Connector
import dzida.server.core.basic.connection.ServerConnection
import dzida.server.core.basic.connection.VerifyingConnectionServer
import dzida.server.core.basic.entity.Id


class FriendServer(
        private val userStore: UserStore,
        private val friendsStore: FriendsStore
) : VerifyingConnectionServer<String, String> {
    private val userConnections: MutableMap<Id<User>, Connector<String>> = hashMapOf()
    private val userTokenVerifier: UserTokenVerifier = UserTokenVerifier()
    private val protocol: JsonProtocol = JsonProtocol.create(ClientCommand.classes, ServerMessage.classes)
    private val userNicks: MutableMap<Id<User>, String> = hashMapOf()

    private fun userNick(userId: Id<User>): String {
        return userNicks.getOrPut(userId, { userStore.getUserNick(userId) })
    }

    private fun establishFriendship(userId1: Id<User>, userId2: Id<User>) {
        if (userId1 == userId2) {
            return
        }
        if (friendsStore.getUserFriends(userId1).contains(userId2)) {
            return
        }
        val friendshipEvent = Friendship(userId1, userId2)
        friendsStore.save(friendshipEvent)
        sendToUser(userId1, FriendshipEstablished(userId2, userNick(userId2)))
        sendToUser(userId2, FriendshipEstablished(userId1, userNick(userId1)))
    }

    private fun sendToUser(userId: Id<User>, message: ServerMessage) {
        val data = protocol.serializeMessage(message)
        if (data != null) {
            userConnections[userId]?.onMessage(data)
        }
    }

    override fun onConnection(connector: Connector<String>, userToken: String): Result {
        val loginToken = userTokenVerifier.verifyToken(EncryptedLoginToken(userToken))
        if (!loginToken.isPresent) {
            return Result.error("Login to is invalid")
        }

        val userId = loginToken.get().userId
        userNicks[userId] = loginToken.get().nick
        userConnections[userId] = connector
        friendsStore.getUserFriends(userId).forEach { friendId ->
            sendToUser(userId, FriendshipEstablished(friendId, userNick(friendId)))
        }

        connector.onOpen(object : ServerConnection<String> {
            override fun send(data: String) {
                val message = protocol.parseMessage(data)
                when (message) {
                    is RequestForFriendShip -> sendToUser(message.userId, FriendshipRequest(userId, userNick(userId)))
                    is AcceptRequest -> establishFriendship(message.userId, userId)
                }
            }

            override fun close() {
                userConnections.remove(userId)
            }
        })

        return Result.ok()
    }


    data class Friendship(val userId1: Id<User>, val userId2: Id<User>) : ServerMessage

    interface ServerMessage {
        companion object {
            val classes: ImmutableSet<Class<*>> = ImmutableSet.of<Class<*>>(
                    ServerMessage.FriendshipRequest::class.java,
                    FriendshipEstablished::class.java)
        }

        data class FriendshipRequest(val userId: Id<User>, val nick: String) : ServerMessage
        data class FriendshipEstablished(val userId: Id<User>, val nick: String) : ServerMessage
    }

    interface ClientCommand {
        companion object {
            val classes: ImmutableSet<Class<*>> = ImmutableSet.of<Class<*>>(
                    ClientCommand.RequestForFriendShip::class.java,
                    ClientCommand.AcceptRequest::class.java)
        }

        data class RequestForFriendShip(val userId: Id<User>) : ClientCommand
        data class AcceptRequest(val userId: Id<User>) : ClientCommand
    }
}