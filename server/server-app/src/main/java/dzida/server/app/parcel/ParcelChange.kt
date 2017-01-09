package dzida.server.app.parcel

import com.google.common.collect.ImmutableSet
import dzida.server.app.basic.entity.Id
import dzida.server.app.instance.GameDefinitions
import dzida.server.app.instance.GameState
import dzida.server.app.instance.event.GameEvent
import dzida.server.app.user.User

interface ParcelChange : GameEvent {
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
    ) : ParcelChange {

        override fun updateState(state: GameState, definitions: GameDefinitions): GameState {
            return state.updateParcels { addChange(this@ParcelClaimed) }
        }
    }
}