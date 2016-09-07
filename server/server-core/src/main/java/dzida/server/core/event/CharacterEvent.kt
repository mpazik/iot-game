package dzida.server.core.event

import dzida.server.core.basic.entity.Id
import dzida.server.core.character.model.Character

interface CharacterEvent : GameEvent {
    val characterId: Id<Character>
}
