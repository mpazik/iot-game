define(function (require, exports, module) {
    const Pixi = require('lib/pixi');
    const MainPlayer = require('../store/main-player');
    const WorldRender = require('./world');
    const MainLoop = require('../store/main-loop');
    const Characters = require('./characters');
    const GroundIndicators = require('./ground-indicators');
    const Projectiles = require('./projectiles');

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
            WorldRender.init();
            container.addChild(WorldRender.tilesLayer);
            container.addChild(WorldRender.eventLayer);

            GroundIndicators.init();
            container.addChild(GroundIndicators.layer);

            //container.addChild(Debug.debugLayer)
            //container.addChild(GameMap.gameObjectLayer)

            Characters.init();
            container.addChild(Characters.layer);
            container.addChild(Characters.pointsLayer);
            container.addChild(Projectiles.layer);

            //container.addChild(GameMap.gameHoveringObjectLayer)
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