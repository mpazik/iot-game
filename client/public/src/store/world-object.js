define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const StoreRegistrar = require('../component/store-registrar');
    const Resources = require('./resources');
    const Messages = require('../component/instance/messages');
    const Dispatcher = require('../component/dispatcher');
    const Timer = require('../component/timer');

    const key = 'worldObject';
    const state = new Map();
    const growingStepDuration = 10000;

    var growObject = null;

    Dispatcher.messageStream.subscribe(Messages.WorldObjectCreated, (event) => {
        addWorldObject(event.worldObject);
    });
    Dispatcher.messageStream.subscribe(Messages.WorldObjectRemoved, (event) => {
        state.delete(event.worldObject.id);
    });

    function addWorldObject(worldObject) {
        const objectData = worldObject.data;
        objectData.id = worldObject.id;
        const objectKind = kindDefinition(objectData.kind);
        if (objectKind['growingSteps']) {
            const growingSteps = objectKind['growingSteps'];
            const createdAgo = Timer.currentTimeOnServer() - objectData.created;
            const stepsAgo = Math.floor(createdAgo / growingStepDuration) + 1;
            const currentStep = Math.min(Math.max(1, stepsAgo), objectKind['growingSteps']);
            objectData.step = currentStep;
            if (currentStep < growingSteps) {
                const timeToNextStep = createdAgo - (stepsAgo * growingSteps);
                for (let i = 1; i <= growingSteps - currentStep; i++) {
                    setTimeout(() => {
                        objectData.step += 1;
                        growObject(objectData)
                    }, timeToNextStep + (i * growingStepDuration));
                }
            }
        }
        state.set(worldObject.id, objectData)
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

    function getRectangleFromLayer(x, y, layer) {
        return {
            x: x + (layer['offsetX'] || 0),
            y: y + (layer['offsetY'] || 0),
            width: layer.width,
            height: layer.height
        }
    }

    function getGroundRectangle(x, y, objectKind) {
        if (objectKind['groundLayer']) {
            return getRectangleFromLayer(x, y, objectKind['groundLayer'])
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
        worldObjectGrown: new Publisher.StreamPublisher((push) => {
            growObject = push;
        }),
        kindDefinition,
        getGroundRectangle,
        isFreePlaceForObject: (x, y, objectKind) => {
            const upperRectangle = objectKind['upperLayer'] ? getRectangleFromLayer(x, y, objectKind['upperLayer']) : null;
            const groundRectangle = getGroundRectangle(x, y, objectKind);
            for (var object of state.values()) {
                const objectKind2 = kindDefinition(object.kind);
                if (upperRectangle && objectKind2['upperLayer']) {
                    const upperRectangle2 = getRectangleFromLayer(object.x, object.y, objectKind2['upperLayer']);
                    if (areRectanglesOverlapping(upperRectangle, upperRectangle2)) {
                        return false;
                    }
                } else {
                    const groundRectangle2 = getGroundRectangle(object.x, object.y, objectKind2);
                    if (areRectanglesOverlapping(groundRectangle, groundRectangle2)) {
                        return false;
                    }
                }
            }
            return true;
        }
    };
});