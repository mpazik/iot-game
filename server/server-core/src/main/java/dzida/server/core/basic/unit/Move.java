package dzida.server.core.basic.unit;

/**
 * Class that represent move. It's a list of a position in times.
 */
public final class Move {
    /**
     * Points represented as array of doubles when x'es are even and y'es are odd.
     */
    private final double[] points;

    /**
     * List of times when corresponding points are achieved.
     * First time means when move has stared. Last one mean when moves will end.
     */
    private final long[] times;

    private Move(double[] points, long times[]) {
        this.points = points;
        this.times = times;
    }

    public static Move fromPosition(long startTime, Point position) {
        double[] points = {position.getX(), position.getY()};
        long[] times = {startTime};
        return new Move(points, times);
    }

    public static Move of(long startTime, double velocity, Point... positions) {
        double[] points = new double[positions.length * 2];
        long[] times = new long[positions.length];
        setPointInTime(points, times, 0, startTime, positions[0].getX(), positions[0].getY());
        for (int i = 1; i < positions.length; i++) {
            addNextPoint(points, times, i, velocity, positions[i].getX(), positions[i].getY());
        }
        return new Move(points, times);
    }

    private static long countDuration(double velocity, double length) {
        return (long) (length / velocity * 1000);
    }

    private static double interpolate(double ratio, double v1, double v2) {
        double dv = v2 - v1;
        return v1 + dv * ratio;
    }

    private static double countSegmentRatio(long[] times, int segment, long time) {
        long segmentStartTime = times[segment - 1];
        long segmentEndTime = times[segment];
        long segmentDuration = segmentEndTime - segmentStartTime;
        long timeInSegment = time - segmentStartTime;
        return (double) timeInSegment / segmentDuration;
    }

    private static void setPointInTime(double[] points, long[] times, int index, long time, double x, double y) {
        points[index * 2] = x;
        points[index * 2 + 1] = y;
        times[index] = time;
    }

    private static void addNextPoint(double[] points, long[] times, int index, double velocity, double x, double y) {
        double previousX = points[(index - 1) * 2];
        double previousY = points[(index - 1) * 2 + 1];
        double length = Geometry2D.distance(previousX, previousY, x, y);
        long duration = countDuration(velocity, length);
        long newTime = times[index - 1] + duration;
        setPointInTime(points, times, index, newTime, x, y);
    }

    public Move continueMoveTo(long time, double velocity, Point... positions) {
        int segment = findSegment(time);

        // this is the case when move to new position starts before move start
        if (segment == 0) {
            return Move.of(time, 1.0, positions);
        }

        int numOfNewPoints = segment + 1 + positions.length; // +1 because segment has -1 offset, + place for new points.
        double[] newPoints = new double[(numOfNewPoints) * 2];
        long[] newTimes = new long[numOfNewPoints];

        // copy old segments
        for (int i = 0; i < segment; i++) {
            setPointInTime(newPoints, newTimes, i, times[i], points[i * 2], points[i * 2 + 1]);
        }

        // point from that new move will begin
        final double x;
        final double y;

        if (segment == times.length) {
            // this is the case when move to new position starts after move finish
            // we need to duplicate last position to create an effect of standing untill move to new position starts.
            x = points[points.length - 2];
            y = points[points.length - 1];
        } else {
            // this is the case when move to new position starts in the middle of move
            // we need to count the point at when move to new position starts
            double segmentRatio = countSegmentRatio(times, segment, time);
            int index = (segment - 1) * 2;
            x = interpolate(segmentRatio, points[index], points[index + 2]);
            y = interpolate(segmentRatio, points[index + 1], points[index + 3]);
        }

        setPointInTime(newPoints, newTimes, segment, time, x, y);
        for (int i = 0; i < positions.length; i++) {
            addNextPoint(newPoints, newTimes, segment + i + 1, velocity, positions[i].getX(), positions[i].getY());
        }

        return new Move(newPoints, newTimes);
    }

    public Move compactHistory(long removeBefore) {
        int segment = findSegment(removeBefore);

        if (segment == 0 || segment == 1) {
            // no segments to remove
            return this;
        }

        if (segment == times.length) {
            double x = points[points.length - 2];
            double y = points[points.length - 1];
            return Move.fromPosition(times[times.length - 1], Point.of(x, y));
        }

        int numberOfPointsThatRemains = times.length - segment + 1;
        double[] newPoints = new double[numberOfPointsThatRemains * 2];
        long[] newTimes = new long[numberOfPointsThatRemains];
        for (int i = 0; i < numberOfPointsThatRemains; i++) {
            int oldIndex = segment - 1 + i;
            setPointInTime(newPoints, newTimes, i, times[oldIndex], points[oldIndex * 2], points[oldIndex * 2 + 1]);
        }
        return new Move(newPoints, newTimes);
    }

    public Point getPositionAtTime(long time) {
        int segment = findSegment(time);
        if (segment == 0) {
            return getStart();
        }
        if (segment == times.length) {
            return getEnd();
        }

        double segmentRatio = countSegmentRatio(times, segment, time);
        int index = (segment - 1) * 2;
        double x = interpolate(segmentRatio, points[index], points[index + 2]);
        double y = interpolate(segmentRatio, points[index + 1], points[index + 3]);
        return Point.of(x, y);
    }

    public double getAngleAtTime(long time) {
        int segment = findSegment(time);
        if (segment == 0 || isSinglePoint()) {
            return 0.0;
        }
        if (segment == times.length) {
            // return the last angle
            segment -= 1;
        }
        int index = segment * 2;
        double dx = points[index] - points[index - 2];
        double dy = points[index + 1] - points[index - 1];
        return -Math.atan2(dx, dy);
    }

    /**
     * Finds the segment on which point is at given time.
     * Segment is a line between points.
     * 0th segment means that the time is before move started
     * 1st segment means that the segment between 0th and 1st point.
     * 2nd segment means that the segment between 1st and 2nd point.
     * ...
     * times.length'th segment means that the time is after move ended
     */
    private int findSegment(long time) {
        for (int i = 0; i < times.length; i++) {
            if (time < times[i]) {
                return i;
            }
        }
        return times.length;
    }

    private boolean isStarted(long time) {
        return time > getStartTime();
    }

    private boolean isEnded(long time) {
        return time > getEndTime();
    }

    public boolean isStanding(long time) {
        return !isStarted(time) || isEnded(time) || isSinglePoint();
    }

    private long getDuration() {
        return getEndTime() - getStartTime();
    }

    private long getStartTime() {
        return times[0];
    }

    private long getEndTime() {
        return times[times.length - 1];
    }

    private Point getStart() {
        return Point.of(points[0], points[1]);
    }

    private Point getEnd() {
        int length = points.length;
        return Point.of(points[length - 2], points[length - 1]);
    }

    private double getLength() {
        return Geometry2D.getLength(points);
    }

    private boolean isSinglePoint() {
        return Geometry2D.isSinglePoint(points);
    }
}
