package dzida.server.app.instance.character

import dzida.server.app.basic.entity.Id
import dzida.server.app.instance.character.model.Character
import java.util.*


class CharacterState private constructor(val state: Map<Id<Character>, Character>) {
    constructor() : this(mapOf())

    fun add(character: Character): CharacterState =
            CharacterState(state.plus(Pair(character.id, character)))

    fun remove(characterId: Id<Character>): CharacterState =
            CharacterState(LinkedHashMap(state).apply { remove(characterId) })

    fun isCharacterLive(characterId: Id<Character>) =
            state.containsKey(characterId)

    fun isCharacterEnemyFor(character1: Id<Character>, character2: Id<Character>) =
            state[character1]!!.type != state[character2]!!.type
}