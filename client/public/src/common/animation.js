define(() => {

    const easeMod = {
        in (deltaFunction) {
            return deltaFunction;
        },
        out(deltaFunction) {
            return (progress) => 1 - deltaFunction(1 - progress);
        },
        inOut(deltaFunction) {
            return (progress) => {
                if (progress < .5)
                    return deltaFunction(2 * progress) / 2;
                else
                    return (2 - deltaFunction(2 * (1 - progress))) / 2
            }
        }
    };

    const ease = {
        linear(progress) {
            return progress
        },

        quad(progress) {
            return Math.pow(progress, 2)
        },
        circle(progress) {
            return 1 - Math.sin(Math.acos(progress))
        },
        bow(progress, x) {
            return Math.pow(progress, 2) * ((x + 1) * progress - x)
        }
    };

    function animationDuration(frames) {
        return frames[frames.length - 1].time
    }

    function countFrameProgress(currentFrame, nextFrame, time) {
        const frameDuration = nextFrame.time - currentFrame.time;
        const timeInFrame = time - currentFrame.time;
        return timeInFrame / frameDuration;
    }

    function findCurrentFrame(frames, time) {
        for (var i = 0; i < frames.length; i++) {
            if (time < frames[i].time) {
                return i - 1;
            }
        }
        return frames.length;
    }

    function interpolateValues(delta, values, nextValues) {
        const obj = {};
        for (const key of Object.keys(values)) {
            if (nextValues.hasOwnProperty(key)) {
                const valueDelta = (nextValues[key] - values[key]) * delta;
                obj[key] = values[key] + valueDelta;
            } else {
                obj[key] = values[key];
            }
        }
        return obj;
    }

    function getValuesAtTime(frames, time) {
        const frameIndex = findCurrentFrame(frames, time);
        if (frameIndex == -1) { // animation didn't start
            return frames[0].values;
        }
        if (frameIndex == frames.length) { // animation ended
            return frames[frames.length - 1].values;
        }
        const currentFrame = frames[frameIndex];
        const nextFrame = frames[frameIndex + 1];

        const frameProgress = countFrameProgress(currentFrame, nextFrame, time);
        const delta = typeof currentFrame.ease == 'function' ? currentFrame.ease(frameProgress) : frameProgress;

        return interpolateValues(delta, currentFrame.values, nextFrame.values);
    }

    class CleanAnimation {

        /**
         * @param frames an animation in format [
         *  {time:0, values:{x: -3, angle: 1}, ease: Animation.easeMod.out(Animation.ease.quad)},
         *  {time:10, values:{x: 7, angle: -10}},
         *  {time:15, values:{x: -3, angle: 14}}
         * ] frames should be ordered by time, first frame should have time equal to 0
         * @param duration - (optional) rescale animation to the given time length in seconds.
         */
        constructor(frames, duration) {
            // there should be frames validation
            this.frames = frames;
            //noinspection AmdModulesDependencies
            this.start = Date.now();
            this.duration = duration || animationDuration(this.frames) * 1000
        }

        getValuesAtTime(time) {
            const timePassed = time - this.start;
            const normalizedProgress = timePassed / this.duration;
            const frameTime = normalizedProgress * animationDuration(this.frames);
            return getValuesAtTime(this.frames, frameTime);
        }

        isFinished(time) {
            const timePassed = time - this.start;
            const normalizedProgress = timePassed / this.duration;
            return normalizedProgress >= 1;
        }
    }

    class ObjectAnimation extends CleanAnimation {

        /**
         * @param object to animate
         * @param frames an animation in format [
         *  {time:0, values:{x: -3, angle: 1}, ease: Animation.easeMod.out(Animation.ease.quad)},
         *  {time:10, values:{x: 7, angle: -10}},
         *  {time:15, values:{x: -3, angle: 14}}
         * ] frames should be ordered by time, first frame should have time equal to 0
         * @param duration - (optional) rescale animation to the given time length in seconds.
         */
        constructor(object, frames, duration) {
            super(frames, duration);
            this.object = object;
        }

        setValuesAtTime(time) {
            const values = this.getValuesAtTime(time);
            console.log(time, values);
            Object.assign(this.object, values);
        }
    }

    return {
        ease,
        easeMod,
        CleanAnimation,
        ObjectAnimation
    }
});
