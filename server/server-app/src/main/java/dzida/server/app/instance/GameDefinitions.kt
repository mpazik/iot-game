package dzida.server.app.instance

import dzida.server.app.basic.entity.Id
import dzida.server.app.instance.skill.Skill
import dzida.server.app.instance.world.`object`.WorldObjectKind


class GameDefinitions(
        private val skills: Map<Id<Skill>, Skill>,
        private val worldObjectKinds: Map<Id<WorldObjectKind>, WorldObjectKind>
) {
    val BotSpeed = 1.0
    val PlayerSpeed = 4.0

    fun getSkill(id: Id<Skill>) = skills[id]!!
    fun getObjectKind(id: Id<WorldObjectKind>) = worldObjectKinds[id]!!
}