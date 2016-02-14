define(function (require, exports, module) {
    const Pixi = require('pixi');
    const WorldObjectStore = require('../store/world-object');
    const tileSize = require('configuration').tileSize;
    const Targeting = require('../component/targeting');
    const Skills = require('../common/model/skills');
    const Dispatcher = require('../component/dispatcher');

    var lowObjectLayer = new Pixi.Container();
    var highObjectLayer = new Pixi.Container();

    const hoverFilter = new Pixi.filters.GrayFilter();
    hoverFilter.gray = -2.0;

    const interactiveFilter = new Pixi.filters.DropShadowFilter();
    interactiveFilter.blur = 10;
    interactiveFilter.angle = 0;
    interactiveFilter.distance = 0;
    interactiveFilter.alpha = 1;

    const worldObjects = [];

    function createWorldObject(objectData) {
        const kind = WorldObjectStore.kindDefinition(objectData.kind);
        const worldObject = Pixi.Sprite.fromImage(kind.sprite);
        worldObject.id = objectData.id;
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
            worldObject.filters = [interactiveFilter, hoverFilter];
        };

        worldObject.mouseout = function () {
            worldObject.filters = [interactiveFilter];
        };

        worldObjects.push(worldObject);
        worldObjects.isHover = kind.isHover;
        if (worldObjects.isHover)
            highObjectLayer.addChild(worldObject);
        else
            lowObjectLayer.addChild(worldObject);
    }

    function makeWorldObjectInteractive(worldObject) {
        worldObject.interactive = true;
        worldObject.filters = [interactiveFilter];
    }

    function makeWorldObjectNonInteractive (worldObject) {
        if (worldObject.interactive) {
            worldObject.interactive = false;
            worldObject.filters = null;
        }
    }

    function removeObject(worldObjectId) {
        const index = worldObjects.findIndex(function(worldObject){
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