package dzida.server.core.world.pathfinding;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import dzida.server.core.basic.unit.Geometry2D;
import dzida.server.core.basic.unit.Line;
import dzida.server.core.basic.unit.Point;

import java.util.List;
import java.util.Optional;

import static dzida.server.core.basic.unit.Geometry2D.*;
import static dzida.server.core.world.pathfinding.BitMapTracker.Direction.*;

class Polygon {
    private final List<Point> points;

    Polygon(List<Point> points) {
        this.points = points;
    }

    public boolean isLineOutside(double l1x, double l1y, double l2x, double l2y) {
        return isLineInPolygon( l1x,  l1y,  l2x,  l2y, false);
    }

    public boolean isLineInside(double l1x, double l1y, double l2x, double l2y) {
        return isLineInPolygon( l1x,  l1y,  l2x,  l2y, true);
    }

    /**
     * Checks if line inside or out side of polygon. Borders are count in to the area.
     */
    private boolean isLineInPolygon(double l1x, double l1y, double l2x, double l2y, boolean inside) {
        if (inside != isInside(l1x, l1y) && !isOnBorder(l1x, l1y)) {
            return false;
        }
        if (inside != isInside(l2x, l2y) && !isOnBorder(l2x, l2y)) {
            return false;
        }

        // is intersection with polygon lines
        int j = points.size() - 1;
        for (int i = 0; i < points.size(); i++) {
            Point bp1 = points.get(j);

            Point bp2 = points.get(i);
            j = i;
            if (isIntersecting(bp1.getX(), bp1.getY(), bp2.getX(), bp2.getY(),
                    l1x, l1y,
                    l2x, l2y)) {
                return false;
            }
        }

        // does line touch the corners
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);

            if (isPointOnLine(p.getX(), p.getY(), l1x, l1y, l2x, l2y)) {
                double lineLength = getLength(l1x, l1y, l2x, l2y);
                if (lineLength == 0) {
                    return true;
                }
                double pointRatio = Geometry2D.distance(l1x, l1y, p.getX(), p.getY()) / lineLength;
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

        int j = points.size() - 1;
        // iterate over all horizontal lines
        // checks number of lines crossing horizontal line on the left side of the point
        // if it's not event then point is inside
        for (int i = 0; i < points.size(); i += 2) {
            double p1y = points.get(j).getY();
            double p2y = points.get(i).getY();
            double lineX = points.get(j).getX();
            j = i + 1;
            if (lineX <= x && Geometry2D.isBetween(y, p1y, p2y)) {
                isInside = !isInside;
            }
        }
        return isInside;
    }

    public boolean isOnBorder(Point point) {
        return isOnBorder(point.getX(), point.getY());
    }

    public boolean isOnBorder(double x, double y) {
        int j = points.size() - 1;
        for (int i = 0; i < points.size(); i += 1) {
            Point p1 = points.get(j);
            Point p2 = points.get(i);
            if (isPointOnLine(x, y,
                    p1.getX(), p1.getY(), p2.getX(), p2.getY())) {
                return true;
            }
            j = i;
        }
        return false;
    }

    public List<Point> getConvexPoints() {
        ImmutableList.Builder<Point> builder = ImmutableList.builder();

        int j = points.size() - 1;
        BitMapTracker.Direction direction = points.get(j).getX() > points.get(j - 1).getX() ? RIGHT : LEFT;

        for (int i = 0; i < points.size(); i += 1) {
            Point point = points.get(j);
            Point next = points.get(i);
            j = i;
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
        BitMapTracker.Direction direction = points.get(j).getX() > points.get(j - 1).getX() ? RIGHT : LEFT;

        for (int i = 0; i < points.size(); i += 1) {
            Point point = points.get(j);
            Point next = points.get(i);
            j = i;
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

    public List<Point> getPoints() {
        return points;
    }

    public List<Point> getIntersections(Line line) {
        ImmutableList.Builder<Point> builder = ImmutableList.builder();

        int j = points.size() - 1;
        for (int i = 0; i < points.size(); i += 1) {
            Point p1 = points.get(j);
            Point p2 = points.get(i);
            j = i;
            Optional<Point> intersection = Geometry2D.getIntersection(p1.getX(), p1.getY(), p2.getX(), p2.getY(),
                    line.getStart().getX(), line.getStart().getY(), line.getEnd().getX(), line.getEnd().getY());
            if (intersection.isPresent()) {
                builder.add(intersection.get());
            }
        }
        return builder.build();
    }
}
