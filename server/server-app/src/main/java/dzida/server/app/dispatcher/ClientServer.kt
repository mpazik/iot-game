package dzida.server.app.dispatcher

import dzida.server.app.user.EncryptedLoginToken
import dzida.server.app.user.User
import dzida.server.app.user.UserTokenVerifier
import dzida.server.core.basic.Result
import dzida.server.core.basic.connection.Connector
import dzida.server.core.basic.connection.ServerConnection
import dzida.server.core.basic.connection.VerifyingConnectionServer
import dzida.server.core.basic.entity.Id

abstract class ClientServer() : VerifyingConnectionServer<String, String> {
    private val userTokenVerifier: UserTokenVerifier = UserTokenVerifier()
    private val userConnections: MutableMap<Id<User>, Connector<String>> = hashMapOf()

    protected fun sendToUser(userId: Id<User>, data: String) {
        userConnections[userId]!!.onMessage(data)
    }

    abstract fun userConnected(userId: Id<User>, nick: String)
    abstract fun userDisconnected(userId: Id<User>)
    abstract fun userSentData(userId: Id<User>, data: String)

    override fun onConnection(connector: Connector<String>, userToken: String): Result {
        val loginToken = userTokenVerifier.verifyToken(EncryptedLoginToken(userToken))
        if (!loginToken.isPresent) {
            return dzida.server.core.basic.Result.error("Login to is invalid")
        }

        val userId = loginToken.get().userId
        userConnections[userId] = connector

        connector.onOpen(UserConnection(this, userId))
        userConnected(userId, loginToken.get().nick)

        return Result.ok()
    }

    protected fun sendToAllUsers(data: String) {
        userConnections.values.forEach { it.onMessage(data) }
    }

    private class UserConnection(val clientServer: ClientServer, val userId: Id<User>) : ServerConnection<String> {
        override fun send(data: String) {
            clientServer.userSentData(userId, data)
        }

        override fun close() {
            clientServer.userDisconnected(userId)
        }
    }
}

