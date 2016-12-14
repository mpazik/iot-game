package dzida.server.app.analytics

import com.google.common.collect.ImmutableSet
import dzida.server.app.basic.entity.Id
import dzida.server.app.dispatcher.ClientServer
import dzida.server.app.protocol.json.JsonProtocol
import dzida.server.app.user.User


class AnalyticsServer(
        private val analyticsStore: AnalyticsStore
) : ClientServer() {
    private val protocol: JsonProtocol = JsonProtocol.create(ClientCommand.classes, ImmutableSet.of())

    override fun userConnected(userId: Id<User>, nick: String) {
    }

    override fun userDisconnected(userId: Id<User>) {
    }

    override fun userSentData(userId: Id<User>, data: String) {
        val message = protocol.parseMessage(data)
        when (message) {
            is ClientCommand.AnalyticEvent -> analyticsStore.saveEvent(userId, message.type)
            is ClientCommand.AnalyticDataEvent -> analyticsStore.saveEvent(userId, message.type, message.data)
        }
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
