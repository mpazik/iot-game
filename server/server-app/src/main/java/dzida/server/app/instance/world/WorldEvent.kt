package dzida.server.app.instance.world

import dzida.server.app.basic.entity.GeneralEntity
import dzida.server.app.instance.GameDefinitions
import dzida.server.app.instance.GameState
import dzida.server.app.instance.event.GameEvent
import dzida.server.app.instance.world.`object`.WorldObject


class WorldObjectCreated(val worldObject: GeneralEntity<WorldObject>) : GameEvent {
    override fun updateState(state: GameState, definitions: GameDefinitions): GameState =
            state.updateWorld { addObject(worldObject.id, worldObject.data) }
                    .updatePositions {
                        val worldObjectData = worldObject.data
                        setWorldObjectCollision(worldObjectData, definitions.getObjectKind(worldObjectData.kind))
                    }
}

class WorldObjectRemoved(val worldObject: GeneralEntity<WorldObject>) : GameEvent {
    override fun updateState(state: GameState, definitions: GameDefinitions): GameState =
            state.updateWorld { removeObject(worldObject.id) }
                    .updatePositions {
                        val worldObjectData = worldObject.data
                        removeWorldObjectCollision(worldObjectData, definitions.getObjectKind(worldObjectData.kind))
                    }
}