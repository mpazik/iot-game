package dzida.server.app.analytics

import com.google.common.collect.ImmutableSet
import dzida.server.app.protocol.json.JsonProtocol
import dzida.server.app.user.EncryptedLoginToken
import dzida.server.app.user.User
import dzida.server.app.user.UserTokenVerifier
import dzida.server.core.basic.Result
import dzida.server.core.basic.connection.Connector
import dzida.server.core.basic.connection.ServerConnection
import dzida.server.core.basic.connection.VerifyingConnectionServer
import dzida.server.core.basic.entity.Id


class AnalyticsServer(
        private val analyticsStore: AnalyticsStore
) : VerifyingConnectionServer<String, String> {
    private val userConnections: MutableMap<Id<User>, Connector<String>> = hashMapOf()
    private val userTokenVerifier: UserTokenVerifier = UserTokenVerifier()
    private val protocol: JsonProtocol = JsonProtocol.create(ClientCommand.classes, ImmutableSet.of())

    override fun onConnection(connector: Connector<String>, userToken: String): Result {
        val loginToken = userTokenVerifier.verifyToken(EncryptedLoginToken(userToken))
        if (!loginToken.isPresent) {
            return Result.error("Login to is invalid")
        }

        val userId = loginToken.get().userId
        userConnections[userId] = connector

        connector.onOpen(object : ServerConnection<String> {
            override fun send(data: String) {
                val message = protocol.parseMessage(data)
                when (message) {
                    is ClientCommand.AnalyticEvent -> analyticsStore.saveEvent(userId, message.type)
                    is ClientCommand.AnalyticDataEvent -> analyticsStore.saveEvent(userId, message.type, message.data)
                }
            }

            override fun close() {
                userConnections.remove(userId)
            }
        })

        return Result.ok()
    }

    interface ClientCommand {
        companion object {
            val classes: ImmutableSet<Class<*>> = ImmutableSet.of<Class<*>>(
                    ClientCommand.AnalyticEvent::class.java,
                    ClientCommand.AnalyticDataEvent::class.java)
        }

        data class AnalyticEvent(val type: String) : ClientCommand
        data class AnalyticDataEvent(val type: String, val data: Any) : ClientCommand
    }
}
