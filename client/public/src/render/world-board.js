define(function (require, exports, module) {
    const Pixi = require('pixi');
    const WorldObjectStore = require('../store/world-object');
    const tileSize = require('configuration').tileSize;
    const zoom = require('configuration').zoom;
    const Targeting = require('../component/targeting');
    const Skills = require('../common/model/skills');
    const Dispatcher = require('../component/dispatcher');

    const boardLayer = new Pixi.Container();

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
        addSprite(worldObject);
        sortDisplayOrder();
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
        boardLayer.removeChild(worldObject);
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

    function addSprite(sprite) {
        boardLayer.addChild(sprite);
    }

    function sortDisplayOrder() {
        boardLayer.children.sort(function (a, b) {
            return (a.position.y + a.height) - (b.position.y + b.height);
        });
    }

    module.exports = {
        init: function () {
            boardLayer.removeChildren();
            const rawWorldObjects = WorldObjectStore.objects();
            rawWorldObjects.forEach(createWorldObject);
        },
        addObject: function (sprite) {
            addSprite(sprite);
        },
        sortDisplayOrder,
        removeObject: function (sprite) {
            boardLayer.removeChild(sprite);
        },
        get boardLayer() {
            return boardLayer;
        }
    };
});