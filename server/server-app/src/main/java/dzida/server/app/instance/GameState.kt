package dzida.server.app.instance

import dzida.server.app.instance.character.CharacterState
import dzida.server.app.instance.parcel.ParcelState
import dzida.server.app.instance.position.PositionState
import dzida.server.app.instance.skill.SkillSate
import dzida.server.app.instance.world.WorldState


class GameState(
        val character: CharacterState,
        val world: WorldState,
        val position: PositionState,
        val skill: SkillSate,
        val parcel: ParcelState) {
    fun updateCharacters(update: CharacterState.() -> CharacterState): GameState {
        return GameState(update(character), world, position, skill, parcel)
    }

    fun updateWorld(update: WorldState.() -> WorldState): GameState {
        return GameState(character, update(world), position, skill, parcel)
    }

    fun updatePositions(update: PositionState.() -> PositionState): GameState {
        return GameState(character, world, update(position), skill, parcel)
    }

    fun updateSkill(update: SkillSate.() -> SkillSate): GameState {
        return GameState(character, world, position, update(skill), parcel)
    }

    fun updateParcels(update: ParcelState.() -> ParcelState): GameState {
        return GameState(character, world, position, skill, update(parcel))
    }
}


