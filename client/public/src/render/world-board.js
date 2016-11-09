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

    const actionIcons = {
        'cut-tree': 'wood-axe',
        'harvest': 'hand'
    };

    function createWorldObject(objectData) {
        const objectKind = WorldObjectStore.kindDefinition(objectData.kind);
        const worldObject = Pixi.Sprite.fromImage(getSprite());
        worldObject.id = objectData.id;
        worldObject.scale = {x: zoom, y: zoom};
        worldObject.position.x = objectData.x * tileSize;
        worldObject.position.y = objectData.y * tileSize;
        worldObject.kind = objectData.kind;

        const action = getObjectAction();
        if (action) {
            worldObject.mousedown = function () {
                Dispatcher.userEventStream.publish({
                    type: 'world-object-targeted',
                    action: {
                        key: action,
                        range: 2,
                        casting: 2000
                    },
                    worldObjectId: worldObject.id,
                    worldObjectKind: objectKind,
                    x: objectData.x + (objectKind['width'] / 2),
                    y: objectData.y + objectKind['height'],
                });
            };
            const groundLayer = objectKind['groundLayer'];
            worldObject.hitArea = new Pixi.Rectangle(
                groundLayer['offsetX'] * tileSize / zoom,
                groundLayer['offsetY'] * tileSize / zoom,
                groundLayer['width'] * tileSize / zoom,
                groundLayer['height'] * tileSize / zoom
            );
            worldObject.mouseover = function () {
                worldObject.filters = [hoverFilter];
                Cursor.setCursor(actionIcons[action]);
            };
            worldObject.mouseout = function () {
                worldObject.filters = null;
                Cursor.setDefault('default');
            };
        }

        if (objectKind['key'] == 'pine' || objectKind['key'] == 'tree' ||
            (objectKind['growingSteps'] && objectKind['growingSteps'] == objectData.step)) {
            worldObject.interactive = true;
        }

        worldObjects.push(worldObject);
        addSprite(worldObject);
        sortDisplayOrder();

        function getObjectAction() {
            switch (objectKind['key']) {
                case 'tree':
                case 'pine':
                    return 'cut-tree';
                case 'tomatoes':
                case 'corn':
                case 'paprika':
                    return 'harvest'
            }
        }

        function getSprite() {
            if (objectKind['growingSteps']) {
                return objectKind['sprite'] + objectData.step + '.png'
            } else {
                return objectKind['sprite'] + '.png'
            }
        }
    }

    function growWorldObject(objectData) {
        const objectKind = WorldObjectStore.kindDefinition(objectData.kind);
        const index = worldObjects.findIndex(function (worldObject) {
            return worldObject.id === objectData.id
        });
        const worldObject = worldObjects[index];
        worldObject.texture = Pixi.Texture.fromFrame(objectKind['sprite'] + objectData.step + '.png');
        if (objectData.step == objectKind['growingSteps']) {
            worldObject.interactive = true;
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
    WorldObjectStore.worldObjectGrown.subscribe(growWorldObject);
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