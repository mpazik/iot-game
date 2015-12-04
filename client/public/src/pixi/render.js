define(function (require, exports, module) {
    const Pixi = require('lib/pixi');
    const MainPlayer = require('../store/main-player');
    const WorldRender = require('./world');
    const MainLoop = require('../store/main-loop');
    const Characters = require('./characters');
    const GroundIndicators = require('./ground-indicators');
    const Projectiles = require('./projectiles');

    var width = 800;
    var height = 600;
    const container = new Pixi.Container();
    const stage = new Pixi.Container();
    stage.addChild(container);
    const renderer = Pixi.autoDetectRenderer(width, height);

    function resize() {
        width = window.innerWidth;
        height = window.innerHeight;
        return renderer.resize(width, height);
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
            element.appendChild(renderer.view);
            resize();
            MainLoop.start();
        },
        clean: function () {
            MainLoop.stop();
            container.removeChildren();
        }
    };
});