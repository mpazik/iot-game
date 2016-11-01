define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const StoreRegistrar = require('../component/store-registrar');
    const Resources = require('./resources');
    const Messages = require('../component/instance/messages');
    const Dispatcher = require('../component/dispatcher');

    const key = 'worldObject';
    const state = new Map();

    Dispatcher.messageStream.subscribe(Messages.WorldObjectCreated, (event) => {
        addWorldObject(event.worldObject);
    });
    Dispatcher.messageStream.subscribe(Messages.WorldObjectRemoved, (event) => {
        state.delete(event.worldObject.id);
    });

    function addWorldObject(worldObject) {
        worldObject.data.id = worldObject.id;
        state.set(worldObject.id, worldObject.data)
    }

    StoreRegistrar.registerStore({
        key,
        state,
        init: (initialState) => {
            state.clear();
            initialState.forEach(addWorldObject);
        }
    });
    const kindDefinition = (kind) => Resources.objectKind(kind);

    function areRectanglesOverlapping(r1, r2) {
        return r1.x < r2.x + r2.width && r1.x + r1.width > r2.x && r1.y < r2.y + r2.height && r1.y + r1.height > r2.y;
    }

    function getCollisionRectangleFromLayer(x, y, layer) {
        return {
            x: x + (layer['offsetX'] || 0),
            y: y + (layer['offsetY'] || 0),
            width: layer.width,
            height: layer.height
        }
    }

    function getCollisionRectangle(x, y, objectKind) {
        if (objectKind['collisionLayer']) {
            return getCollisionRectangleFromLayer(x, y, objectKind['collisionLayer'])
        }
        return {
            x,
            y: y,
            width: objectKind.width,
            height: objectKind.height
        }
    }

    module.exports = {
        key,
        objects: () => Array.from(state.values()),
        worldObjectCreated: new Publisher.StreamPublisher((push) => {
            Dispatcher.messageStream.subscribeLast(Messages.WorldObjectCreated, (event) => push(event.worldObject.data));
        }),
        worldObjectRemoved: new Publisher.StreamPublisher((push) => {
            Dispatcher.messageStream.subscribeLast(Messages.WorldObjectRemoved, (event) => push(event.worldObject.id));
        }),
        kindDefinition,
        isFreePlaceForObject: (x, y, objectKind) => {
            const treeCollisionRectangle = objectKind['treeCollisionLayer'] ? getCollisionRectangleFromLayer(x, y, objectKind['treeCollisionLayer']) : null;
            const collisionRectangle = getCollisionRectangle(x, y, objectKind);
            for (var object of state.values()) {
                const objectKind2 = kindDefinition(object.kind);
                if (treeCollisionRectangle && objectKind2['treeCollisionLayer']) {
                    const collisionRectangle2 = getCollisionRectangleFromLayer(object.x, object.y, objectKind2['treeCollisionLayer']);
                    if (areRectanglesOverlapping(treeCollisionRectangle, collisionRectangle2)) {
                        return false;
                    }
                } else {
                    const collisionRectangle2 = getCollisionRectangle(object.x, object.y, objectKind2);
                    if (areRectanglesOverlapping(collisionRectangle, collisionRectangle2)) {
                        return false;
                    }
                }
            }
            return true;
        }
    };
});