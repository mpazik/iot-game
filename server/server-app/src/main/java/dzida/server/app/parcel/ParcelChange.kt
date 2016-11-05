package dzida.server.app.parcel

import com.google.common.collect.ImmutableSet
import dzida.server.app.user.User
import dzida.server.core.basic.entity.Id

interface ParcelChange {
    companion object {
        val classes: ImmutableSet<Class<*>> = ImmutableSet.of<Class<*>>(
                ParcelClaimed::class.java)
    }

    data class ParcelClaimed(
            val x: Int,
            val y: Int,
            val owner: Id<User>,
            val ownerName: String,
            val parcelName: String
    ) : ParcelChange
}