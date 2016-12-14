package dzida.server.app.parcel

import com.google.common.collect.ImmutableSet
import dzida.server.app.basic.entity.Id
import dzida.server.app.instance.command.InstanceCommand
import dzida.server.app.user.User

interface ParcelCommand : InstanceCommand {
    companion object {
        val classes: ImmutableSet<Class<*>> = ImmutableSet.of<Class<*>>(
                ClaimParcel::class.java)
    }

    data class ClaimParcel(
            val x: Int,
            val y: Int,
            val owner: Id<User>,
            val ownerName: String,
            val parcelName: String
    ) : ParcelCommand
}
