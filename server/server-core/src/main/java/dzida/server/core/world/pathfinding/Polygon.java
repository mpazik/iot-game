package dzida.server.core.world.pathfinding;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dzida.server.core.basic.unit.Geometry2D;
import dzida.server.core.basic.unit.Line;
import dzida.server.core.basic.unit.Point;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static dzida.server.core.world.pathfinding.BitMapTracker.Direction.*;

class Polygon {
    private final List<Point> points;

    Polygon(List<Point> points) {
        this.points = points;
    }

    public boolean intersect(Line line) {
        Point point1 = line.getStart();
        boolean isStartInside = isInside(point1.getX(), point1.getY()) && !isOnBorder(line.getStart());
        if (isStartInside) {
            return true;
        }
        Point point = line.getEnd();
        boolean isEndInside = isInside(point.getX(), point.getY()) && !isOnBorder(line.getEnd());
        if (isEndInside) {
            return true;
        }

        int j = points.size() - 1;

        if (intersectionWithPolygonLines(line, j)) {
            return true;
        }

        for (int i = 0; i < points.size(); i++) {
            Point p1 = points.get(i);

            if (line.isPointOnLine(p1)) {
                double lineLength = line.length();
                if (lineLength == 0) {
                    return false;
                }
                double pointRatio = line.getStart().distanceTo(p1) / line.length();
                Point probe1 = line.interpolate(Math.max(0, pointRatio - 0.001));
                if (pointRatio > 0 && !isOnBorder(probe1) && isInside(probe1.getX(), probe1.getY())) {
                    return true;
                }
                Point probe2 = line.interpolate(Math.min(1, pointRatio + 0.001));
                if (pointRatio < 1 && !isOnBorder(probe2) && isInside(probe2.getX(), probe2.getY())) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean intersectInside(Line line) {
        Point point1 = line.getStart();
        boolean isStartOutside = !isInside(point1.getX(), point1.getY()) && !isOnBorder(line.getStart());
        if (isStartOutside) {
            return true;
        }
        Point point = line.getEnd();
        boolean isEndOutside = !isInside(point.getX(), point.getY()) && !isOnBorder(line.getEnd());
        if (isEndOutside) {
            return true;
        }

        int j = points.size() - 1;

        if (intersectionWithPolygonLines(line, j)) {
            return true;
        }

        for (int i = 0; i < points.size(); i++) {
            Point p1 = points.get(i);

            if (line.isPointOnLine(p1)) {
                double lineLength = line.length();
                if (lineLength == 0) {
                    return false;
                }
                double pointRatio = line.getStart().distanceTo(p1) / lineLength;
                Point probe1 = line.interpolate(Math.max(0, pointRatio - 0.001));
                if (pointRatio > 0 && !isOnBorder(probe1) && !isInside(probe1.getX(), probe1.getY())) {
                    return true;
                }
                Point probe2 = line.interpolate(Math.min(1, pointRatio + 0.001));
                if (pointRatio < 1 && !isOnBorder(probe2) && !isInside(probe2.getX(), probe2.getY())) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean intersectionWithPolygonLines(Line line, int j) {
        for (int i = 0; i < points.size(); i++) {
            Point p1 = points.get(j);

            Point p2 = points.get(i);
            j = i;
            if (line.isIntersecting(new Line(p1, p2))) {
                return true;
            }
        }
        return false;
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
        int j = points.size() - 1;
        for (int i = 0; i < points.size(); i += 1) {
            Line line = new Line(points.get(j), points.get(i));
            if (line.isPointOnLine(point)) {
                return true;
            }
            j = i;
        }
        return false;
    }

    public Set<Point> getConvexPoints() {
        ImmutableSet.Builder<Point> builder = ImmutableSet.builder();

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

    public Set<Point> getConcavePoints() {
        Set<Point> points = new HashSet<>(this.points);
        points.removeAll(getConvexPoints());
        return ImmutableSet.copyOf(points);
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
            Line border = new Line(points.get(j), points.get(i));
            j = i;
            Optional<Point> intersection = border.getIntersection(line);
            if (intersection.isPresent()) {
                builder.add(intersection.get());
            }
        }
        return builder.build();
    }
}
