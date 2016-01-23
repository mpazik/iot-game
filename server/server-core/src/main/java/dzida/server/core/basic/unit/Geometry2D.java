package dzida.server.core.basic.unit;

import java.util.Optional;

public class Geometry2D {
    private Geometry2D() {
        //no instance
    }

    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(distanceSqr(x1, y1, x2, y2));
    }

    public static double distanceSqr(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return dx * dx + dy * dy;
    }

    public static double getLength(double[] points) {
        double length = 0;
        if (isSinglePoint(points)) {
            return 0;
        }
        for (int i = 0; i < points.length - 2; i += 2) {
            length += Geometry2D.distance(points[i], points[i + 1], points[i + 2], points[i + 3]);
        }
        return length;
    }

    public static double crossProduct(double p1x, double p1y, double p2x, double p2y) {
        return p1x * p2y - p1y * p2x;
    }

    /**
     * Checks if point is on the line. Line edge doesn't count as line.
     */
    public static boolean isPointOnLine(double px, double py, double lx1, double ly1, double lx2, double ly2) {
        return isBetweenOrOn(px, lx1, lx2) && isBetweenOrOn(py, ly1, ly2) &&
                crossProduct(lx2 - lx1, ly2 - ly1, px - lx1, py - ly1) == 0.0;
    }

    public Point interpolate(double lx1, double ly1, double lx2, double ly2, double ratio) {
        return Point.of(lx1 + ((lx2 - lx1) * ratio), ly1 + ((ly2 - ly1) * ratio));
    }

    // It's based on: http://martin-thoma.com/how-to-check-if-two-line-segments-intersect/
    // It's 2x faster than getIntersection due to primitives.
    /**
     * Checks lines intersect each other. Touching is not intersecting.
     */
    public static boolean isIntersecting(double px1, double py1, double px2, double py2,
                                         double qx1, double qy1, double qx2, double qy2) {
        double rx = px2 - px1;
        double ry = py2 - py1;
        double sx = qx2 - qx1;
        double sy = qy2 - qy1;
        double rxs = crossProduct(rx, ry, sx, sy);
        if (rxs == 0) {
            return false; // Lines are parallel.
        }

        double qpx = qx1 - px1;
        double qpy = qy1 - py1;

        double ratioR = crossProduct(qpx, qpy, sx, sy) / rxs;
        double ratioS = crossProduct(qpx, qpy, rx, ry) / rxs;

        return (ratioR > 0 && ratioR < 1) && (ratioS > 0 && ratioS < 1);

    }

    /// It's based on: http://stackoverflow.com/a/14143738/292237
    /**
     * Get the intersection point of two lines. Touching is not intersecting.
     */
    public static Optional<Point> getIntersection(double px, double py, double px2, double py2,
                                                  double qx, double qy, double qx2, double qy2) {
        double rx = px2 - px;
        double ry = py2 - py;
        double sx = qx2 - qx;
        double sy = qy2 - qy;
        double rxs = crossProduct(rx, ry, sx, sy);
        if (rxs == 0) {
            return Optional.empty(); // Lines are parallel.
        }

        double qpx = qx - px;
        double qpy = qy - py;

        double ratioR = crossProduct(qpx, qpy, sx, sy) / rxs;
        double ratioS = crossProduct(qpx, qpy, rx, ry) / rxs;

        if ((0 < ratioR && ratioR < 1) && (0 < ratioS && ratioS < 1)) {
            return Optional.of(Point.of(qx + sx * ratioS, qy + sy * ratioS));
        }

        return Optional.empty();
    }

    public static boolean isSinglePoint(double[] points) {
        return points.length == 2;
    }

    public static int cordToTail(double cord) {
        return (int) Math.floor(cord);
    }

    public static boolean isBetweenOrOn(double num, double a, double b) {
        return num == a || num == b || isBetween(num, a, b);
    }

    /**
     * Behaviour on the edges is undefined
     */
    public static boolean isBetween(double num, double a, double b) {
        return num >= a != num >= b;
    }

    @FunctionalInterface
    public interface IntPointOperator {
        void apply(int x, int y);
    }
}
