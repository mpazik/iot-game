define(function (require, exports, module) {
    const Pixi = require('pixi');
    const WorldObjectStore = require('../store/world-object');
    const tileSize = require('configuration').tileSize;
    const zoom = require('configuration').zoom;
    const Dispatcher = require('../component/dispatcher');
    const Cursor = require('../store/cursor');
    const Timer = require('../component/timer');

    const boardLayer = new Pixi.Container();

    const hoverFilter = new Pixi.filters.GrayFilter();
    hoverFilter.gray = -2.0;

    const worldObjects = [];
    const animatedWorldObjects = [];

    const actionIcons = {
        'cut-tree': 'wood-axe',
        'harvest': 'hand',
        'cook': 'cooking-pot',
    };

    function createWorldObject(objectData) {
        const objectKind = WorldObjectStore.kindDefinition(objectData.kind);
        const worldObject = getSprite();
        if (objectKind['animationSteps']) {
            animatedWorldObjects.push(worldObject);
        }
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

        if (objectKind['key'] == 'pine' || objectKind['key'] == 'tree' || objectKind['key'] == 'campfire' ||
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
                case 'tomato':
                case 'corn':
                case 'paprika':
                    return 'harvest';
                case 'campfire':
                    return 'cook';
            }
        }

        function getSprite() {
            if (objectKind['animationSteps']) {
                return new AnimatedSprite(objectKind['sprite'], objectKind['animationSteps'], objectData.created)
            }
            if (objectKind['growingSteps']) {
                return Pixi.Sprite.fromImage(objectKind['sprite'] + objectData.step + '.png');
            }
            return Pixi.Sprite.fromImage(objectKind['sprite'] + '.png');
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

        if (worldObject.kind['animationSteps']) {
            const index = animatedWorldObjects.findIndex(function (worldObject) {
                return worldObject.id === worldObjectId
            });
            animatedWorldObjects.splice(index, 1);
        }
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

    function AnimatedSprite(textureName, frameNumber, startTime) {
        this.frames = [];
        this.frameNumber = frameNumber;
        this.startTime = startTime || 0;

        for (var i = 1; i <= frameNumber; i++) {
            this.frames.push(Pixi.Texture.fromFrame(textureName + i + '.png'));
        }

        Pixi.Sprite.call(this, this.frames[0]);
    }

    AnimatedSprite.prototype = Object.create(Pixi.Sprite.prototype);
    AnimatedSprite.prototype._getCurrentFrame = function (time) {
        return Math.floor((time - this.startTime) / 150);
    };
    AnimatedSprite.prototype.update = function (time) {
        const frameIndex = this._getCurrentFrame(time) % this.frameNumber;
        //noinspection JSUnusedGlobalSymbols
        this.texture = this.frames[frameIndex]
    };

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
        updateAnimatedObjects () {
            const time = Timer.currentTimeOnServer();
            animatedWorldObjects.forEach(obj => {
                obj.update(time)
            })
        },
        get boardLayer() {
            return boardLayer;
        }
    };
});