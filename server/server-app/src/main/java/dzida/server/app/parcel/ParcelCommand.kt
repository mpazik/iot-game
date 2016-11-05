package dzida.server.app.parcel

import com.google.common.collect.ImmutableSet
import dzida.server.app.user.User
import dzida.server.core.basic.entity.Id

interface ParcelCommand {
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
