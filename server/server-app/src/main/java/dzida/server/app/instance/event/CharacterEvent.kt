package dzida.server.app.instance.event

import dzida.server.app.basic.entity.Id
import dzida.server.app.instance.character.model.Character

interface CharacterEvent : GameEvent {
    val characterId: Id<Character>
}
