define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const StoreRegistrar = require('../component/store-registrar');
    const Resources = require('./resources');
    const Messages = require('../component/instnace/messages');
    const Dispatcher = require('../component/dispatcher');

    const key = 'worldObject';
    const state = new Map();

    Dispatcher.messageStream.subscribe(Messages.WorldObjectCreated, (event) => {
        addWorldObject(event.worldObject);
    });
    Dispatcher.messageStream.subscribe(Messages.WorldObjectRemoved, (event) => {
        state.delete(event.worldObjectId);
    });

    function addWorldObject(worldObject) {
        worldObject.data.id = worldObject.id;
        state.set(worldObject.id, worldObject.data)
    }

    StoreRegistrar.registerStore({
        key,
        state: () => Map.toObject(state),
        init: (initialState) => {
            state.clear();
            initialState.forEach(addWorldObject);
        }
    });
    const kindDefinition = (kind) => Resources.objectKind(kind);

    function isTileOnObject(tx, ty, object) {
        const kind = kindDefinition(object.kind);
        for (var i = 0; i < kind.width; i++) {
            for (var j = 0; j < kind.height; j++) {
                var currentTileX = object.x + i;
                var currentTileY = object.y + j;
                if (currentTileX == tx && currentTileY == ty) {
                    return true;
                }
            }
        }
        return false;
    }

    module.exports = {
        key,
        objects: () => Array.from(state.values()),
        worldObjectCreated: new Publisher.StreamPublisher((push) => {
            Dispatcher.messageStream.subscribeLast(Messages.WorldObjectCreated, (event) => push(event.worldObject.data));
        }),
        worldObjectRemoved: new Publisher.StreamPublisher((push) => {
            Dispatcher.messageStream.subscribeLast(Messages.WorldObjectRemoved, (event) => push(event.worldObjectId));
        }),
        kindDefinition,
        isAnyObjectOnTile: (tx, ty) => {
            for (var obj of state.values()) {
                if (isTileOnObject(tx, ty, obj)) return true;
            }
            return false;
        }
    };
});