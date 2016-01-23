define(function (require, exports, module) {

    function findSegment(times, time) {
        for (var i = 0; i < times.length; i++) {
            if (time < times[i]) {
                return i;
            }
        }
        return times.length;
    }

    function interpolate(ratio, v1, v2) {
        const dv = v2 - v1;
        return v1 + dv * ratio;
    }

    function countSegmentRatio(times, segment, time) {
        const segmentStartTime = times[segment - 1];
        const segmentEndTime = times[segment];
        const segmentDuration = segmentEndTime - segmentStartTime;
        const timeInSegment = time - segmentStartTime;
        return timeInSegment / segmentDuration;
    }

    function getStart(points) {
        return {x: points[0], y: points[1]};
    }

    function getEnd(points) {
        const length = points.length;
        return {x: points[length - 2], y: points[length - 1]};
    }

    return {
        positionAtTime: (move, time) => {
            const segment = findSegment(move.times, time);
            if (segment == 0) {
                return getStart(move.points);
            }
            if (segment == move.times.length) {
                return getEnd(move.points);
            }

            const segmentRatio = countSegmentRatio(move.times, segment, time);
            const index = (segment - 1) * 2;
            const x = interpolate(segmentRatio, move.points[index], move.points[index + 2]);
            const y = interpolate(segmentRatio, move.points[index + 1], move.points[index + 3]);
            return {x, y};
        },
        angleAtTime: (move, time) => {
            var segment = findSegment(move.times, time);
            if (segment == 0 || move.times.length == 0) {
                return 0.0;
            }
            if (segment == move.times.length) {
                // return the last angle
                segment -= 1;
            }
            const index = segment * 2;
            const dx = move.points[index - 2] - move.points[index];
            const dy = move.points[index - 1] - move.points[index + 1];
            return -Math.atan2(dx, dy)
        }
    };
});

