package dzida.server.app.instance.skill

import dzida.server.app.basic.entity.Id
import dzida.server.app.instance.character.model.Character
import java.util.*

class SkillSate private constructor(val state: Map<Id<Character>, SkillData>) {
    constructor() : this(mapOf())

    fun add(characterId: Id<Character>, skillData: SkillData): SkillSate =
            SkillSate(state.plus(Pair(characterId, skillData)))

    fun remove(characterId: Id<Character>): SkillSate =
            SkillSate(LinkedHashMap(state).apply { remove(characterId) })

    fun setCharacterCooldown(characterId: Id<Character>, skill: Skill, timestamp: Long): SkillSate {
        val newCooldownTill = timestamp + skill.cooldown
        val newSkillData = state[characterId]!!.updateCooldown(newCooldownTill)
        return SkillSate(state.plus(Pair(characterId, newSkillData)))
    }

    fun changeCharacterHealth(characterId: Id<Character>, change: Int): SkillSate {
        val skillData = state[characterId]!!
        val newHealth = skillData.health + change
        val newSkillData = skillData.updateHealth(newHealth)
        return SkillSate(state.plus(Pair(characterId, newSkillData)))
    }

    fun isOnCooldown(casterId: Id<Character>, time: Long): Boolean {
        return time < state[casterId]!!.cooldownTill
    }

    fun getHealth(characterId: Id<Character>): Int {
        return state[characterId]!!.health
    }

    fun getMaxHealth(characterId: Id<Character>): Int {
        return state[characterId]!!.maxHealth
    }

    fun getInitialSkillData(characterType: Int): SkillData {
        if (characterType == Character.Type.Bot) {
            return SkillData(20, 20, 0)
        }
        if (characterType == Character.Type.Player) {
            return SkillData(300, 300, 0)
        }
        throw IllegalStateException("If this is throw that means there is time to change implementation to use enums")
    }

    class SkillData(val health: Int, val maxHealth: Int, val cooldownTill: Long) {
        fun updateHealth(health: Int) = SkillData(health, maxHealth, cooldownTill)
        fun updateCooldown(cooldownTill: Long) = SkillData(health, maxHealth, cooldownTill)
    }
}