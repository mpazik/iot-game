define(function (require, exports, module) {
    const Pixi = require('pixi');
    const WorldStore = require('../store/world');
    const ResourcesStore = require('../store/resources');
    const Dispatcher = require('../component/dispatcher');
    const tileSize = require('configuration').tileSize;
    const Publisher = require('../common/basic/publisher');

    function createTileTextures(tileset) {
        var fileName = tileset.image;
        var tileTextures = [];
        var texture = Pixi.Texture.fromImage("tilesets/" + fileName);
        for (var i = 0; i < texture.height / tileSize; i++) {
            for (var j = 0; j < texture.width / tileSize; j++) {
                var tileTexture = new Pixi.Texture(texture, {
                    x: j * tileSize,
                    y: i * tileSize,
                    width: tileSize,
                    height: tileSize
                });
                tileTextures.push(tileTexture);
            }
        }
        return tileTextures;
    }

    function createTilesLayer(tileTextures, worldState) {
        var width = worldState.width;
        var height = worldState.height;
        var tilesLayer = new Pixi.Container();
        for (var tx = 0; tx < width; tx++) {
            for (var ty = 0; ty < height; ty++) {
                var tile = WorldStore.tile(tx, ty);
                if (tile == 0) {
                    continue;
                }
                // tile kinds starts from 1, texture for them starts from 0
                var sprite = new Pixi.Sprite(tileTextures[tile - 1]);
                sprite.position.x = tx * tileSize;
                sprite.position.y = ty * tileSize;
                tilesLayer.addChild(sprite);
            }
        }
        tilesLayer.cacheAsBitmap = true;
        return tilesLayer;
    }

    var tilesLayer = new Pixi.Container();
    var eventLayer = new Pixi.Container();

    module.exports = {
        init: function () {
            var worldState = WorldStore.state();
            tilesLayer.removeChildren();
            var tileset = ResourcesStore.tileset(worldState.tileset);
            var tileTexture = createTileTextures(tileset);
            tilesLayer.addChild(createTilesLayer(tileTexture, worldState));
            eventLayer.interactive = true;
            eventLayer.hitArea = new Pixi.Rectangle(0, 0, worldState.width * tileSize, worldState.height * tileSize);
        },
        worldMapClicked: new Publisher.StatePublisher({x: 0, y: 0}, push => {
            eventLayer.mousedown = function (data) {
                data.stopPropagation();
                var cords = eventLayer.toLocal(data.data.global);
                var tileCordX = cords.x / tileSize;
                var tileCordY = cords.y / tileSize;
                Dispatcher.userEventStream.publish({
                    type: 'map-clicked',
                    x: tileCordX,
                    y: tileCordY
                });
                push({x :tileCordX, y: tileCordY})
            };
        }),
        mousePositionStream: new Publisher.StatePublisher({x: 0, y: 0}, push => {
            eventLayer.mousemove = function (data) {
                data.stopPropagation();
                var cords = eventLayer.toLocal(data.data.global);
                var tileCordX = cords.x / tileSize;
                var tileCordY = cords.y / tileSize;
                push({
                    x: tileCordX,
                    y: tileCordY
                });
            };
        }),
        get tilesLayer() {
            return tilesLayer;
        },
        get eventLayer() {
            return eventLayer;
        }
    };
});