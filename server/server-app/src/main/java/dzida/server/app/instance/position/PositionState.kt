package dzida.server.app.instance.position

import dzida.server.app.basic.entity.Id
import dzida.server.app.basic.unit.BitMap
import dzida.server.app.basic.unit.Move
import dzida.server.app.basic.unit.Point
import dzida.server.app.instance.character.model.Character
import dzida.server.app.instance.world.`object`.WorldObject
import dzida.server.app.instance.world.`object`.WorldObjectKind
import dzida.server.app.instance.world.pathfinding.CollisionMapFactory
import dzida.server.app.instance.world.pathfinding.PathFinder
import java.util.*

class PositionState private constructor(
        private val collisionBitMap: BitMap,
        private val pathFinder: PathFinder,
        val state: Map<Id<Character>, Move>
) {
    constructor(collisionBitMap: BitMap) : this(collisionBitMap, PathFinder(CollisionMapFactory(5).createCollisionMap(collisionBitMap)), mapOf())

    fun put(characterId: Id<Character>, move: Move): PositionState =
            PositionState(collisionBitMap, pathFinder, state.plus(Pair(characterId, move)))

    fun remove(characterId: Id<Character>): PositionState =
            PositionState(collisionBitMap, pathFinder, LinkedHashMap(state).apply { remove(characterId) })

    fun setWorldObjectCollision(worldObject: WorldObject, worldObjectKind: WorldObjectKind) =
            setWorldObjectCollision(worldObject, worldObjectKind, true)

    fun removeWorldObjectCollision(worldObject: WorldObject, worldObjectKind: WorldObjectKind) =
            setWorldObjectCollision(worldObject, worldObjectKind, false)

    fun areCharactersInDistance(character1: Id<Character>, character2: Id<Character>, distance: Double, time: Long): Boolean {
        val char1Pos = state[character1]!!.getPositionAtTime(time)
        val char2Pos = state[character2]!!.getPositionAtTime(time)
        return char1Pos.isInRange(char2Pos, distance)
    }

    fun getMove(characterId: Id<Character>) = state[characterId]!!

    fun findPathToDestination(characterId: Id<Character>, destination: Point, time: Long): List<Point> {
        val move = getMove(characterId)
        val currentPosition = move.getPositionAtTime(time)
        return pathFinder.findPathToDestination(currentPosition, destination)
    }

    fun getInitialMove(spawnPoint: Point, currentTime: Long, playerSpeed: Double): Move {
        return Move.of(currentTime, playerSpeed, spawnPoint)
    }

    private fun setWorldObjectCollision(worldObject: WorldObject, worldObjectKind: WorldObjectKind, value: Boolean): PositionState {
        if (!worldObjectKind.isCollidable) {
            return this
        }
        val groundLayer = worldObjectKind.groundLayer
        if (groundLayer != null) {
            val startX = worldObject.x + groundLayer.offsetX
            val startY = worldObject.y + groundLayer.offsetY
            return setRectangleCollision(startX, groundLayer.width, startY, groundLayer.height, value)
        } else {
            return setRectangleCollision(worldObject.x, worldObjectKind.width, worldObject.y, worldObjectKind.height, value)
        }

    }

    private fun setRectangleCollision(startX: Int, width: Int, startY: Int, height: Int, value: Boolean): PositionState {
        val collisionBitMapBuilder = BitMap.ImmutableBitMap.builder(collisionBitMap)
        for (x in startX..startX + width - 1) {
            for (y in startY..startY + height - 1) {
                collisionBitMapBuilder.set(x, y, value)
            }
        }
        val newBitMap = collisionBitMapBuilder.build()
        val collisionMap = CollisionMapFactory(5).createCollisionMap(newBitMap)
        val newPathFinder = PathFinder(collisionMap)
        return PositionState(newBitMap, newPathFinder, state)
    }
}