package dzida.server.app.instance

import dzida.server.app.basic.entity.GeneralEntity
import dzida.server.app.basic.entity.Id
import dzida.server.app.instance.character.model.Character
import dzida.server.app.instance.event.GameEvent
import dzida.server.app.instance.position.event.CharacterMoved
import dzida.server.app.instance.skill.SkillSate
import dzida.server.app.map.descriptor.Scenario
import dzida.server.app.user.User
import java.util.*
import java.util.function.Consumer
import javax.ws.rs.NotSupportedException

class StateSynchroniser(private val instance: Instance, private val scenario: Scenario) {
    private val listeners = HashMap<Id<User>, Consumer<GameEvent>>()

    fun registerCharacter(listenerId: Id<User>, send: Consumer<GameEvent>) {
        listeners.put(listenerId, send)
        sendInitialPacket(listenerId)
    }

    fun unregisterListener(listenerId: Id<User>) {
        listeners.remove(listenerId)
    }

    fun sendInitialPacket(userId: Id<User>) {
        val initialData = InitialData(prepareData(instance.state), scenario)
        listeners[userId]!!.accept(initialData)
    }

    fun syncStateChange(gameEvent: GameEvent) {
        listeners.values.forEach { consumer -> consumer.accept(gameEvent) }
    }

    class InitialData(val state: Map<String, Any>, @Suppress("unused") val scenario: Scenario) : GameEvent {
        override fun updateState(state: GameState, definitions: GameDefinitions): GameState {
            throw NotSupportedException()
        }
    }

    fun prepareData(state: GameState): Map<String, Any> {
        return mapOf(
                Pair("character", state.character.state.values),
                Pair("skill", state.skill.state.entries.map({ SkillCharacterState(it.key, it.value) })),
                Pair("move", state.position.state.entries.map({ CharacterMoved(it.key, it.value) })),
                Pair("world", state.world.worldMap),
                Pair("worldObject", state.world.worldObjects.entries.map({ GeneralEntity(it.key, it.value) })),
                Pair("parcel", state.parcel.parcelChanges)
        )
    }

    class SkillCharacterState(val characterId: Id<Character>, @Suppress("unused") val skillData: SkillSate.SkillData)
}



