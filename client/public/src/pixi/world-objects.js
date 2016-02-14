define(function (require, exports, module) {
    const Pixi = require('pixi');
    const WorldObjectStore = require('../store/world-object');
    const tileSize = require('configuration').tileSize;

    var lowObjectLayer = new Pixi.Container();
    var highObjectLayer = new Pixi.Container();

    function createObject(objectData) {
        const kind = WorldObjectStore.kindDefinition(objectData.kind);
        const sprite = Pixi.Sprite.fromImage(kind.sprite);
        sprite.position.x = objectData.x * tileSize;
        sprite.position.y = objectData.y * tileSize;

        if(kind.isHover)
            highObjectLayer.addChild(sprite);
        else
            lowObjectLayer.addChild(sprite);
    }

    WorldObjectStore.objectCreated.subscribe(createObject);

    module.exports = {
        init: function () {
            WorldObjectStore.objects().forEach(createObject);
        },
        get lowObjectLayer() {
            return lowObjectLayer;
        },
        get highObjectLayer() {
            return highObjectLayer;
        }
    };
});