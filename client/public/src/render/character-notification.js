define((require) => {
    const Pixi = require('pixi');
    const CharacterNotification = require('../store/character-notification');
    const MainPlayer = require('../store/main-player');
    const Animation = require('../common/animation');
    const MainLoop = require('../store/main-loop');

    var runningAnimations = [];

    const layer = new Pixi.Container();
    const font = {
        font: "16px Arial",
        fill: 0xFFEE88,
        stroke: 0x000000,
        strokeThickness: 2
    };

    function runAnimation() {
        //noinspection AmdModulesDependencies
        const time = Date.now();
        runningAnimations.forEach(animation => animation.setValuesAtTime(time));
        const finishedAnimations = runningAnimations.filter(animation => animation.isFinished(time));
        finishedAnimations.forEach(animation => layer.removeChild(animation.object));
        runningAnimations = runningAnimations.filter(animation => !animation.isFinished(time));

        if (runningAnimations.length == 0) {
            MainLoop.renderStream.unsubscribe(runAnimation)
        }
    }

    CharacterNotification.publisher.subscribe((text) => {
        const position = MainPlayer.positionInPixels;
        const notification = new Pixi.Text(text, font);
        notification.position.x = position.x - notification.width / 2;
        layer.addChild(notification);

        const animation = new Animation.ObjectAnimation(notification, [
            {time: 0, values: {y: position.y - 20}},
            {time: 1, values: {y: position.y - 60}}
        ]);
        if (runningAnimations.length == 0) {
            MainLoop.renderStream.subscribe(runAnimation);
        }
        runningAnimations.push(animation);
    });

    return {
        layer
    }
});

