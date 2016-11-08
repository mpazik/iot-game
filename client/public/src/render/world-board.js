define(function (require, exports, module) {
    const Pixi = require('pixi');
    const WorldObjectStore = require('../store/world-object');
    const tileSize = require('configuration').tileSize;
    const zoom = require('configuration').zoom;
    const Dispatcher = require('../component/dispatcher');
    const Cursor = require('../store/cursor');

    const boardLayer = new Pixi.Container();

    const hoverFilter = new Pixi.filters.GrayFilter();
    hoverFilter.gray = -2.0;

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
                type: 'world-object-targeted',
                worldObjectId: worldObject.id,
                action: {
                    key: 'cut-tree',
                    range: 2,
                    casting: 2000
                },
                x: objectData.x + (objectKind['width'] / 2),
                y: objectData.y + objectKind['height'],
            });
        };

        if (objectKind['key'] == 'pine' || objectKind['key'] == 'tree') {
            worldObject.interactive = true;
            const groundLayer = objectKind['groundLayer'];
            worldObject.hitArea = new Pixi.Rectangle(
                groundLayer['offsetX'] * tileSize / zoom,
                groundLayer['offsetY'] * tileSize / zoom,
                groundLayer['width'] * tileSize / zoom,
                groundLayer['height'] * tileSize / zoom
            );
            worldObject.mouseover = function () {
                worldObject.filters = [hoverFilter];
                Cursor.setCursor('wood-axe');
            };
            worldObject.mouseout = function () {
                worldObject.filters = null;
                Cursor.setDefault('default');
            };
        }

        worldObjects.push(worldObject);
        addSprite(worldObject);
        sortDisplayOrder();
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