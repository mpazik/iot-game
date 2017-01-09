package dzida.server.app.instance.world

import dzida.server.app.basic.entity.GeneralEntity
import dzida.server.app.basic.entity.Id
import dzida.server.app.basic.unit.Point
import dzida.server.app.instance.world.`object`.WorldObject
import dzida.server.app.instance.world.`object`.WorldObjectKind
import dzida.server.app.instance.world.map.WorldMap
import java.time.Instant
import java.util.*


class WorldState private constructor(
        val worldObjects: Map<Id<WorldObject>, WorldObject>,
        val worldMap: WorldMap
) {
    constructor(worldMap: WorldMap) : this(mapOf(), worldMap)

    fun addObject(worldObjectId: Id<WorldObject>, worldObject: WorldObject): WorldState =
            WorldState(worldObjects.plus(Pair(worldObjectId, worldObject)), worldMap)

    fun removeObject(worldObjectId: Id<WorldObject>): WorldState =
            WorldState(LinkedHashMap(worldObjects).apply { remove(worldObjectId) }, worldMap)

    fun getObject(id: Id<WorldObject>) = GeneralEntity(id, worldObjects[id]!!)

    fun getSpawnPoint(): Point = worldMap.spawnPoint

    fun isEmpty() = worldObjects.isEmpty()

    fun createWorldObject(objectKind: Id<WorldObjectKind>, x: Int, y: Int, currentTime: Long): GeneralEntity<WorldObject> {
        val worldObject = WorldObject(objectKind, x, y, Instant.ofEpochMilli(currentTime))
        return GeneralEntity(
                Id(newId()),
                worldObject
        )
    }

    private fun newId(): Long {
        return (worldObjects.keys.map { it.value }.max() ?: 0) + 1
    }
}