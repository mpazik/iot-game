define(function (require, exports, module) {
    const Pixi = require('pixi');
    const WorldObjectStore = require('../store/world-object');
    const tileSize = require('configuration').tileSize;
    const zoom = require('configuration').zoom;
    const Targeting = require('../component/targeting');
    const Skills = require('../common/model/skills');
    const Dispatcher = require('../component/dispatcher');

    var lowObjectLayer = new Pixi.Container();
    var highObjectLayer = new Pixi.Container();

    const worldObjects = [];

    function createWorldObject(objectData) {
        const objectKind = WorldObjectStore.kindDefinition(objectData.kind);
        const worldObject = Pixi.Sprite.fromImage(objectKind['sprite'] + '.png');
        worldObject.id = objectData.id;
        worldObject.scale = {x: zoom, y: zoom};
        worldObject.position.x = objectData.x * tileSize;
        worldObject.position.y = objectData.y * tileSize;
        worldObject.kind = objectData.kind;

        worldObject.mousedown = function () {
            Dispatcher.userEventStream.publish({
                type: 'world-object-clicked',
                worldObjectId: worldObject.id
            });
        };

        worldObject.mouseover = function () {
        };

        worldObject.mouseout = function () {
        };

        worldObjects.push(worldObject);

        // const collisionLayer = objectKind['collisionLayer'];
        // const graphics = new Pixi.Graphics();
        // graphics.lineStyle(5, 0xFFFF00);
        // graphics.drawRect(
        //     objectData.x * tileSize,
        //     objectData.y * tileSize,
        //     objectKind.width * tileSize,
        //     objectKind.height * tileSize);
        // highObjectLayer.addChild(graphics);
        //
        // if (objectKind['collisionLayer']) {
        //     const collisionLayer = objectKind['collisionLayer'];
        //     const collisionGraphics = new Pixi.Graphics();
        //     collisionGraphics.lineStyle(4, 0xFF0000);
        //     const collisionX = objectData.x + (collisionLayer['offsetX'] || 0);
        //     const collisionY = objectData.y + (collisionLayer['offsetY'] || 0);
        //     collisionGraphics.drawRect(
        //         collisionX * tileSize,
        //         collisionY * tileSize,
        //         collisionLayer['width'] * tileSize,
        //         collisionLayer['height'] * tileSize);
        //     highObjectLayer.addChild(collisionGraphics);
        // }
        //
        // if (objectKind['treeCollisionLayer']) {
        //     const collisionLayer = objectKind['treeCollisionLayer'];
        //     const collisionGraphics = new Pixi.Graphics();
        //     collisionGraphics.lineStyle(4, 0x00FF00);
        //     const collisionX = objectData.x + (collisionLayer['offsetX'] || 0);
        //     const collisionY = objectData.y + (collisionLayer['offsetY'] || 0);
        //     collisionGraphics.drawRect(
        //         collisionX * tileSize,
        //         collisionY * tileSize,
        //         collisionLayer['width'] * tileSize,
        //         collisionLayer['height'] * tileSize);
        //     highObjectLayer.addChild(collisionGraphics);
        // }

        worldObjects.isHover = objectKind.isHover;
        if (worldObjects.isHover)
            highObjectLayer.addChild(worldObject);
        else
            lowObjectLayer.addChild(worldObject);
    }

    function makeWorldObjectInteractive(worldObject) {
        worldObject.interactive = true;
        worldObject.filters = [interactiveFilter];
    }

    function makeWorldObjectNonInteractive(worldObject) {
        if (worldObject.interactive) {
            worldObject.interactive = false;
            worldObject.filters = null;
        }
    }

    function removeObject(worldObjectId) {
        const index = worldObjects.findIndex(function (worldObject) {
            return worldObject.id === worldObjectId
        });
        const worldObject = worldObjects[index];
        if (worldObjects.isHover)
            highObjectLayer.removeChild(worldObject);
        else
            lowObjectLayer.removeChild(worldObject);
        worldObjects.splice(index, 1);
    }

    WorldObjectStore.worldObjectCreated.subscribe(createWorldObject);
    WorldObjectStore.worldObjectRemoved.subscribe(removeObject);

    Targeting.targetingState.subscribe(function (skill) {
        worldObjects.forEach(worldObject => makeWorldObjectNonInteractive(worldObject));
        if (skill !== null && skill.type === Skills.Types.GATHER) {
            worldObjects
                .filter(worldObject => worldObject.kind == skill.worldObject)
                .forEach(worldObject => makeWorldObjectInteractive(worldObject));
        }
    });

    module.exports = {
        init: function () {
            const rawWorldObjects = WorldObjectStore.objects();
            rawWorldObjects.forEach(createWorldObject);
        },
        get lowObjectLayer() {
            return lowObjectLayer;
        },
        get highObjectLayer() {
            return highObjectLayer;
        }
    };
});