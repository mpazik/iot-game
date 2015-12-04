define(function (require, exports, module) {
    var Publisher = require('../common/basic/publisher');
    var renderingTime = 0.0;
    var sumRenderingTime = 0.0;
    var framesRenderingInSum = 0;
    var frames = 0;
    var lastSecoundTime = 0;
    var lastFrameTime = 0;
    var fps = 0;
    var running = false;
    var publishRender = null;
    var publishStats = null;
    var renderStream = new Publisher.StreamPublisher(function (fn) {
        return publishRender = fn;
    });
    var updateStatsStream = new Publisher.StreamPublisher(function (fn) {
        return publishStats = fn;
    });

    function updateRendering() {
        if (!running) {
            return;
        }

        var newTime = performance.now();
        const delta = newTime - lastFrameTime;
        lastFrameTime = newTime;
        //fps
        frames += 1;
        if (newTime > lastSecoundTime + 1000) {
            fps = Math.round((frames * 1000) / (newTime - lastSecoundTime) * 100) / 100;
            lastSecoundTime = newTime;
            frames = 0;
        }
        //loop and performance
        publishRender(delta);
        renderingTime = performance.now() - newTime;
        sumRenderingTime += renderingTime;
        framesRenderingInSum += 1;
        requestAnimationFrame(updateRendering);
    }

    function updateStats() {
        if (!running) {
            return;
        }
        var renderingFrameTime = sumRenderingTime / framesRenderingInSum;
        publishStats({fps, renderingFrameTime});
        sumRenderingTime = 0;
        framesRenderingInSum = 0;
        setTimeout(updateStats, 1000);
    }

    module.exports = {
        stop: function () {
            running = false;
        },
        togglePause: function () {
            if (running) {
                this.stop();
            }
            else {
                this.start();
            }
        },
        start: function () {
            running = true;
            lastSecoundTime = performance.now();
            updateRendering();
            updateStats();
        },
        get renderStream() {
            return renderStream;
        },
        get updateStatsStream() {
            return updateStatsStream;
        }
    };
});