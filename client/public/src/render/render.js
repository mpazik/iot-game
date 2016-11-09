define(function (require, exports, module) {
    const Pixi = require('pixi');
    const Animation = require('../common/animation');
    const MainPlayer = require('../store/main-player');
    const World = require('./world');
    const MainLoop = require('../store/main-loop');
    const Characters = require('./characters');
    const GroundIndicators = require('./ground-indicators');
    const Projectiles = require('./projectiles');
    const WorldBoard = require('./world-board');
    const ParcelIndicator = require('./parcel-indicator');
    const CharacterNotification = require('./character-notification');
    require('./building-indicator');

    var runningEffects = [];
    const EffectAnimations = {
        twist: [
            {time: 0, values: {angle: 0}, ease: Animation.ease.quad},
            {time: 12, values: {angle: 8}},
            {time: 16, values: {angle: 16}},
            {time: 24, values: {angle: 0}, ease: Animation.easeMod.out(Animation.ease.quad)},
        ],
        invert: [
            {time: 0, values: {invert: 0}, ease: Animation.easeMod.in(Animation.ease.quad)},
            {time: 5, values: {invert: -2}, ease: Animation.easeMod.inOut(Animation.ease.quad)},
            {time: 15, values: {invert: 8}, ease: Animation.easeMod.out(Animation.ease.quad)},
            {time: 20, values: {invert: 0}},
        ]
    };

    const Filters = {
        twist: Object.assign(new Pixi.filters.TwistFilter(), {radius: 1.0, angle: 0}),
        invert: Object.assign(new Pixi.filters.InvertFilter(), {invert: 0}),
    };

    var width = window.innerWidth;
    var height = window.innerHeight;
    const container = new Pixi.Container();
    const stage = new Pixi.Container();
    stage.addChild(container);
    Pixi.SCALE_MODES.DEFAULT = Pixi.SCALE_MODES.NEAREST;
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
        WorldBoard.updateAnimatedObjects();
        container.position.x = Math.round(-MainPlayer.positionInPixels.x + width / 2);
        container.position.y = Math.round(-MainPlayer.positionInPixels.y + height / 2);
        renderer.render(stage);
    }

    MainLoop.renderStream.subscribe(render);

    function runAnimation() {
        //noinspection AmdModulesDependencies
        const time = Date.now();
        runningEffects.forEach(animation => animation.setValuesAtTime(time));
        runningEffects = runningEffects.filter(animation => !animation.isFinished(time));
        const filters = runningEffects.map(animation => animation.object);
        container.filters = filters.length == 0 ? null : filters;

        if (runningEffects.length == 0) {
            MainLoop.renderStream.unsubscribe(runAnimation)
        }
    }

    module.exports = {
        init: function (element) {
            element.appendChild(renderer.view);
            renderer.render(stage);
        },
        runEffect(effect) {
            const filter = Filters[effect];
            const animationFrames = EffectAnimations[effect];
            if (filter == null || animationFrames == null) {
                throw `Effect ${effect} is undefined`
            }

            if (runningEffects.find(animation => animation.effect == effect)) {
                return;
            }
            const animation = new Animation.ObjectAnimation(filter, animationFrames);
            animation.effect = effect;
            if (runningEffects.length == 0) {
                MainLoop.renderStream.subscribe(runAnimation);
            }
            runningEffects.push(animation);
        },
        initWorld: function () {
            World.init();
            WorldBoard.init();
            GroundIndicators.init();
            Characters.init();

            container.addChild(World.tilesLayer);
            container.addChild(World.eventLayer);
            container.addChild(WorldBoard.boardLayer);
            container.addChild(ParcelIndicator.layer);
            container.addChild(CharacterNotification.layer);
            container.addChild(GroundIndicators.layer);
            container.addChild(Projectiles.layer);
            resize();
            MainLoop.start();
        },
        cleanWorld: function () {
            MainLoop.stop();
            container.removeChildren(0, container.children.length);
            renderer.render(stage);
        }
    };
});