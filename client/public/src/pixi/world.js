define(function (require, exports, module) {
    const Pixi = require('pixi');
    const WorldStore = require('../store/world');
    const ResourcesStore = require('../store/resources');
    const Dispatcher = require('../component/dispatcher');
    const tileSize = require('configuration').tileSize;
    const Targeting = require('../component/targeting');

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
        var tiles = worldState.tiles;
        var tilesLayer = new Pixi.Container();
        for (var tx = 0; tx < width; tx++) {
            for (var ty = 0; ty < height; ty++) {
                var tileKind = tiles[ty * width + tx];
                if (tileKind == 0) {
                    continue;
                }
                // tile kinds starts from 1, texture for them starts from 0
                var sprite = new Pixi.Sprite(tileTextures[tileKind - 1]);
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

    Targeting.targetingState.subscribe(function (skill) {
        //noinspection RedundantIfStatementJS
        if (skill === null) {
            // skill was deactivated so map is click-able again
            eventLayer.interactive = true;
        } else {
            // targeting on. Map should not be click-able right now.
            eventLayer.interactive = false;
        }
    });

    module.exports = {
        init: function () {
            var worldState = WorldStore.state();
            tilesLayer.removeChildren();
            var tileset = ResourcesStore.tileset(worldState.tileset);
            var tileTexture = createTileTextures(tileset);
            tilesLayer.addChild(createTilesLayer(tileTexture, worldState));
            eventLayer.interactive = true;
            eventLayer.hitArea = new Pixi.Rectangle(0, 0, worldState.width * tileSize, worldState.height * tileSize);
            eventLayer.mousedown = function (data) {
                data.stopPropagation();
                var cords = eventLayer.toLocal(data.data.global);
                var tileCordX = cords.x / tileSize;
                var tileCordY = cords.y / tileSize;
                var tileX = Math.floor(tileCordX);
                var tileY = Math.floor(tileCordY);
                var type = tileset.terrains[worldState.tiles[tileX][tileY]];
                Dispatcher.userEventStream.publish({
                    type: 'map-clicked',
                    x: tileCordX,
                    y: tileCordY,
                    tileX,
                    tileY,
                    tileType: type
                });
            };


        },
        get tilesLayer() {
            return tilesLayer;
        },
        get eventLayer() {
            return eventLayer;
        }
    };
});