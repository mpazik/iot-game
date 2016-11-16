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

    const actionIcons = {
        'cut-tree': 'wood-axe',
        'harvest': 'hand',
        'cook': 'cooking-pot',
        'travel': 'hand',
        'talk': 'hand',
    };

    function createWorldObject(objectData) {
        const objectKind = WorldObjectStore.kindDefinition(objectData.kind);
        const worldObject = getSprite();
        if (objectKind['animationSteps']) {
            worldObject.animated = true;
            worldObject.frameDuration = objectKind['frameDuration'] || 500;
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
            const groundRectangle = WorldObjectStore.getGroundRectangle(0, 0, objectKind);
            worldObject.hitArea = new Pixi.Rectangle(
                groundRectangle.x * tileSize / zoom,
                groundRectangle.y * tileSize / zoom,
                groundRectangle.width * tileSize / zoom,
                groundRectangle.height * tileSize / zoom
            );
            worldObject.mouseover = function () {
                worldObject.filters = [hoverFilter];
                Cursor.setCursor(actionIcons[action]);
            };
            worldObject.mouseout = function () {
                worldObject.filters = null;
                Cursor.setDefault('default');
            };

            // make not riped plants non interactive
            if (!objectKind['growingSteps'] || objectKind['growingSteps'] == objectData.step) {
                worldObject.interactive = true;
            }
        }

        if (objectKind['decay']) {
            addDecayBar();
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
                case 'cave-entrance':
                    return 'travel';
                case 'ladder':
                    return 'travel';
                case 'programmer':
                    return 'talk';
            }
        }

        function addDecayBar() {
            const decayDuration = objectKind['decay'] * 1000;
            const liveTime = Timer.currentTimeOnServer() - objectData.created;
            const decayBarLength = objectKind['width'] * tileSize / zoom;
            const decayBarHeight = 4;
            const border = 1;
            if (liveTime >= decayDuration) {
                // this is some kind of error. This object shouldn't exist any more or time is counted badly.
                return
            }
            const decayBar = new Pixi.Container();
            decayBar.objectId = objectData.id;
            decayBar.position = {x: objectData.x * tileSize, y: (objectData.y + objectKind['height'] ) * tileSize};
            decayBar.scale = {x: zoom, y: zoom};
            var decayBarBg = new Pixi.Graphics();
            decayBarBg.beginFill(0x000000, 1);
            decayBarBg.drawRect(0, 0, decayBarLength, decayBarHeight);
            decayBar.addChild(decayBarBg);
            var decayBarInside = new Pixi.Graphics();
            decayBarInside.beginFill(0x4088FF, 1);
            decayBarInside.drawRect(0, 0, decayBarLength - (2 * border), decayBarHeight - (2 * border));
            decayBarInside.position = {x: border, y: border};
            decayBar.inside = decayBarInside;
            decayBar.addChild(decayBarInside);
            worldObject.decay = {
                bar: decayBar,
                update(time) {
                    const liveTime = time - objectData.created;
                    const decayPercent = liveTime / decayDuration;
                    if (decayPercent >= 1) {
                        decayBarInside.width = 0;
                    } else {
                        decayBarInside.width = (decayBarLength - (2 * border)) * (1 - decayPercent);
                    }
                }
            };
            addSprite(decayBar);
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

        if (worldObject.decay) {
            boardLayer.removeChild(worldObject.decay.bar);
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
        return Math.floor((time - this.startTime) / this.frameDuration);
    };
    AnimatedSprite.prototype.update = function (time) {
        const frameIndex = this._getCurrentFrame(time) % this.frameNumber;
        //noinspection JSUnusedGlobalSymbols
        this.texture = this.frames[frameIndex]
    };

    module.exports = {
        init: function () {
            Cursor.setDefault('default');
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
            worldObjects.forEach(obj => {
                if (obj.animated) {
                    obj.update(time)
                }
                if (obj.decay) {
                    obj.decay.update(time);
                }
            })
        },
        get boardLayer() {
            return boardLayer;
        }
    };
});