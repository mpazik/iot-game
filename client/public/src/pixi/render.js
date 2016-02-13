define(function (require, exports, module) {
    const Pixi = require('pixi');
    const MainPlayer = require('../store/main-player');
    const World = require('./world');
    const MainLoop = require('../store/main-loop');
    const Characters = require('./characters');
    const GroundIndicators = require('./ground-indicators');
    const BuildingIndicator = require('./building-indicator');
    const Projectiles = require('./projectiles');
    const WorldObjects = require('./world-objects');

    var width = window.innerWidth;
    var height = window.innerHeight;
    const container = new Pixi.Container();
    const stage = new Pixi.Container();
    stage.addChild(container);
    const renderer = Pixi.autoDetectRenderer(width, height);
    renderer.backgroundColor = 0x156c99;

    function resize() {
        width = window.innerWidth;
        height = window.innerHeight;
        renderer.resize(width, height);
    }

    window.onresize = resize;

    function render() {
        Characters.recalculatePositions();
        container.position.x = Math.round(-MainPlayer.positionInPixels.x + width / 2);
        container.position.y = Math.round(-MainPlayer.positionInPixels.y + height / 2);
        renderer.render(stage);
    }

    MainLoop.renderStream.subscribe(render);

    module.exports = {
        init: function (element) {
            element.appendChild(renderer.view);
            renderer.render(stage);
        },
        initWorld: function () {
            World.init();
            WorldObjects.init();
            GroundIndicators.init();
            Characters.init();

            container.addChild(World.tilesLayer);
            container.addChild(GroundIndicators.layer);
            container.addChild(WorldObjects.lowObjectLayer);
            container.addChild(BuildingIndicator.lowLayer);
            container.addChild(World.eventLayer);
            container.addChild(Characters.layer);
            container.addChild(Characters.pointsLayer);
            container.addChild(Projectiles.layer);
            container.addChild(WorldObjects.highObjectLayer);
            container.addChild(BuildingIndicator.highLayer);
            resize();
            MainLoop.start();
        },
        cleanWorld: function () {
            MainLoop.stop();
            container.removeChildren();
            renderer.render(stage);
        }
    };
});