package dzida.server.core.world.pathfinding;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import dzida.server.core.basic.unit.Point;
import dzida.server.core.basic.unit.PointList;

import java.util.List;
import java.util.Optional;

import static dzida.server.core.basic.unit.Geometry2D.distance;
import static dzida.server.core.basic.unit.Geometry2D.getIntersection;
import static dzida.server.core.basic.unit.Geometry2D.getLength;
import static dzida.server.core.basic.unit.Geometry2D.interpolate;
import static dzida.server.core.basic.unit.Geometry2D.isBetween;
import static dzida.server.core.basic.unit.Geometry2D.isIntersecting;
import static dzida.server.core.basic.unit.Geometry2D.isPointOnLine;
import static dzida.server.core.world.pathfinding.BitMapTracker.Direction.BOTTOM;
import static dzida.server.core.world.pathfinding.BitMapTracker.Direction.LEFT;
import static dzida.server.core.world.pathfinding.BitMapTracker.Direction.RIGHT;
import static dzida.server.core.world.pathfinding.BitMapTracker.Direction.TOP;

class Polygon {
    private final PointList points;

    Polygon(PointList points) {
        this.points = points;
    }

    public PointList getPoints() {
        return points;
    }

    public boolean isLineOutside(double l1x, double l1y, double l2x, double l2y) {
        return isLineInPolygon(l1x, l1y, l2x, l2y, false);
    }

    public boolean isLineInside(double l1x, double l1y, double l2x, double l2y) {
        return isLineInPolygon(l1x, l1y, l2x, l2y, true);
    }

    /**
     * Checks if line inside or out side of polygon. Borders are count in to the area.
     */
    private boolean isLineInPolygon(double l1x, double l1y, double l2x, double l2y, boolean inside) {
        // is intersection with polygon lines
        for (int i = 0, j = points.size() - 1; i < points.size(); j = i, i += 1) {
            if (isIntersecting(points.x(j), points.y(j), points.x(i), points.y(i), l1x, l1y, l2x, l2y)) {
                return false;
            }
        }

        // does line touch the border
        if (inside != isInside(l1x, l1y) && !isOnBorder(l1x, l1y)) {
            return false;
        }
        if (inside != isInside(l2x, l2y) && !isOnBorder(l2x, l2y)) {
            return false;
        }

        // does line touch the corners
        for (int i = 0; i < points.size(); i++) {
            double px = points.x(i);
            double py = points.y(i);
            if (isPointOnLine(px, py, l1x, l1y, l2x, l2y)) {
                double lineLength = getLength(l1x, l1y, l2x, l2y);
                if (lineLength == 0) {
                    return true;
                }
                double pointRatio = distance(l1x, l1y, px, py) / lineLength;
                Point probe1 = interpolate(l1x, l1y, l2x, l2y, Math.max(0, pointRatio - 0.001));
                if (pointRatio > 0 && !isOnBorder(probe1.getX(), probe1.getY()) && inside != isInside(probe1.getX(), probe1.getY())) {
                    return false;
                }
                Point probe2 = interpolate(l1x, l1y, l2x, l2y, Math.min(1, pointRatio + 0.001));
                if (pointRatio < 1 && !isOnBorder(probe2.getX(), probe2.getY()) && inside != isInside(probe2.getX(), probe2.getY())) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Check if tile is inside polygon.
     */
    public boolean isInsideTile(int tileX, int tileY) {
        // added epsilon since is isInside doesn't count polygon borders
        return isInside(tileX + 0.5, tileY + 0.5);
    }

    /**
     * Check if point is inside the polygon. If points are on the boarder the behaviour is undefined.
     */
    public boolean isInside(double x, double y) {
        boolean isInside = false;

        // iterate over all horizontal lines
        // checks number of lines crossing horizontal line on the left side of the point
        // if it's not event then point is inside
        for (int i = 0, j = points.size() - 1; i < points.size(); j = i + 1, i += 2) {
            if (points.x(j) <= x && isBetween(y, points.y(j), points.y(i))) {
                isInside = !isInside;
            }
        }
        return isInside;
    }

    public boolean isOnBorder(Point point) {
        return isOnBorder(point.getX(), point.getY());
    }

    public boolean isOnBorder(double x, double y) {
        for (int i = 0, j = points.size() - 1; i < points.size(); j = i, i += 1) {
            if (isPointOnLine(x, y, points.x(j), points.y(j), points.x(i), points.y(i))) {
                return true;
            }
        }
        return false;
    }

    public List<Point> getConvexPoints() {
        ImmutableList.Builder<Point> builder = ImmutableList.builder();

        int j = points.size() - 1;
        BitMapTracker.Direction direction = points.x(j) > points.x(j - 1) ? RIGHT : LEFT;

        for (int i = 0; i < points.size(); j = i, i += 1) {
            Point point = points.getPoint(j);
            Point next = points.getPoint(i);
            switch (direction) {
                case TOP:
                    if (point.getX() < next.getX()) {
                        builder.add(point);
                        direction = RIGHT;
                    } else {
                        direction = LEFT;
                    }
                    break;
                case RIGHT:
                    if (point.getY() < next.getY()) {
                        builder.add(point);
                        direction = BOTTOM;
                    } else {
                        direction = TOP;
                    }
                    break;
                case BOTTOM:
                    if (point.getX() > next.getX()) {
                        builder.add(point);
                        direction = LEFT;
                    } else {
                        direction = RIGHT;
                    }
                    break;
                case LEFT:
                    if (point.getY() > next.getY()) {
                        builder.add(point);
                        direction = TOP;
                    } else {
                        direction = BOTTOM;
                    }
                    break;
            }
        }

        return builder.build();
    }

    public List<Point> getConcavePoints() {
        ImmutableList.Builder<Point> builder = ImmutableList.builder();

        int j = points.size() - 1;
        BitMapTracker.Direction direction = points.x(j) > points.x(j - 1) ? RIGHT : LEFT;

        for (int i = 0; i < points.size(); j = i, i += 1) {
            Point point = points.getPoint(j);
            Point next = points.getPoint(i);
            switch (direction) {
                case TOP:
                    if (point.getX() < next.getX()) {
                        direction = RIGHT;
                    } else {
                        builder.add(point);
                        direction = LEFT;
                    }
                    break;
                case RIGHT:
                    if (point.getY() < next.getY()) {
                        direction = BOTTOM;
                    } else {
                        builder.add(point);
                        direction = TOP;
                    }
                    break;
                case BOTTOM:
                    if (point.getX() > next.getX()) {
                        direction = LEFT;
                    } else {
                        builder.add(point);
                        direction = RIGHT;
                    }
                    break;
                case LEFT:
                    if (point.getY() > next.getY()) {
                        direction = TOP;
                    } else {
                        builder.add(point);
                        direction = BOTTOM;
                    }
                    break;
            }
        }

        return builder.build();
    }

    @Override
    public String toString() {
        return "Polygon{" +
                "points=" + points +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Polygon polygon = (Polygon) o;
        return Objects.equal(points, polygon.points);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(points);
    }

    public PointList getIntersections(double l1x, double l1y, double l2x, double l2y) {
        PointList.Builder builder = PointList.builder();

        for (int i = 0, j = points.size() - 1; i < points.size(); j = i, i += 1) {
            Optional<Point> intersection = getIntersection(points.x(j), points.y(j), points.x(i), points.y(i), l1x, l1y, l2x, l2y);
            if (intersection.isPresent()) {
                Point point = intersection.get();
                builder.add(point);
            }
        }
        return builder.build();
    }
}
