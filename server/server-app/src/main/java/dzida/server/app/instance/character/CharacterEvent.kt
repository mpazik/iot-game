package dzida.server.app.instance.character

import dzida.server.app.basic.entity.Id
import dzida.server.app.basic.unit.Move
import dzida.server.app.instance.GameDefinitions
import dzida.server.app.instance.GameState
import dzida.server.app.instance.character.model.Character
import dzida.server.app.instance.event.CharacterEvent
import dzida.server.app.instance.skill.SkillSate


class CharacterSpawned(val character: Character, val initialMove: Move, val initialSkillData: SkillSate.SkillData) : CharacterEvent {
    override fun updateState(state: GameState, definitions: GameDefinitions) =
            state.updateCharacters { add(character) }
                    .updatePositions { put(character.id, initialMove) }
                    .updateSkill { add(character.id, initialSkillData) }


    override val characterId: Id<Character>
        get() = character.id
}

class CharacterDied(override val characterId: Id<Character>) : CharacterEvent {
    override fun updateState(state: GameState, definitions: GameDefinitions) =
            state.updateCharacters { remove(characterId) }
                    .updatePositions { remove(characterId) }
                    .updateSkill { remove(characterId) }
}