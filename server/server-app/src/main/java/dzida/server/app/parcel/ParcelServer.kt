package dzida.server.app.parcel

import dzida.server.app.dispatcher.ClientServer
import dzida.server.app.protocol.json.JsonProtocol
import dzida.server.app.serialization.BasicJsonSerializer
import dzida.server.app.user.User
import dzida.server.core.basic.entity.Id


class ParcelServer(private val parcelStore: ParcelStore) : ClientServer() {
    private val protocol: JsonProtocol = JsonProtocol.create(BasicJsonSerializer.getSerializer(), ParcelCommand.classes, ParcelChange.classes)

    override fun userConnected(userId: Id<User>, nick: String) {
        parcelStore.getParcelChanges().forEach {
            val message = protocol.serializeMessage(it)
            if (message != null) {
                sendToUser(userId, message)
            }
        }
    }

    override fun userDisconnected(userId: Id<User>) {
    }

    override fun userSentData(userId: Id<User>, data: String) {
        val command = protocol.parseMessage(data)
        if (command is ParcelCommand.ClaimParcel) {
            applyChange(ParcelChange.ParcelClaimed(command.x, command.y, command.owner, command.ownerName, command.parcelName))
        }
    }

    private fun applyChange(change: ParcelChange) {
        parcelStore.saveChange(change)
        val message = protocol.serializeMessage(change)
        sendToAllUsers(message!!)
    }
}

