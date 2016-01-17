package dzida.server.core.world.pathfinding;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import dzida.server.core.basic.unit.Line;
import dzida.server.core.basic.unit.Point;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dzida.server.core.basic.unit.Points.cordToTail;
import static dzida.server.core.world.pathfinding.BitMapTracker.Direction.*;

class Polygon {
    private final List<Point> points;

    Polygon(List<Point> points) {
        this.points = points;
    }

    public boolean intersect(Line line) {
        boolean isStartInside = isInside(line.getStart()) && !isOnBorder(line.getStart());
        if (isStartInside) {
            return true;
        }
        boolean isEndInside = isInside(line.getEnd()) && !isOnBorder(line.getEnd());
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
                if (pointRatio > 0 && !isOnBorder(probe1) && isInside(probe1)) {
                    return true;
                }
                Point probe2 = line.interpolate(Math.min(1, pointRatio + 0.001));
                if (pointRatio < 1 && !isOnBorder(probe2) && isInside(probe2)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean intersectInside(Line line) {
        boolean isStartOutside = !isInside(line.getStart()) && !isOnBorder(line.getStart());
        if (isStartOutside) {
            return true;
        }
        boolean isEndOutside = !isInside(line.getEnd()) && !isOnBorder(line.getEnd());
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
                if (pointRatio > 0 && !isOnBorder(probe1) && !isInside(probe1)) {
                    return true;
                }
                Point probe2 = line.interpolate(Math.min(1, pointRatio + 0.001));
                if (pointRatio < 1 && !isOnBorder(probe2) && !isInside(probe2)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean intersectionWithPolygonLines(Line line, int j) {
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

    boolean isInside(int x, int y) {
        boolean isInside = false;

        int j = points.size() - 1;
        for (int i = 0; i < points.size(); i += 2) {
            int p1y = (int) points.get(j).getY();
            int p2y = (int) points.get(i).getY();
            int lineX = (int) points.get(j).getX();
            j = i + 1;
            if (lineX <= x && y >= Math.min(p1y, p2y) && y < Math.max(p1y, p2y)) {
                isInside = !isInside;
            }
        }
        return isInside;
    }

    boolean isInside(Point point) {
        return isInside(cordToTail(point.getX()), cordToTail(point.getY()));
    }

    boolean isOnBorder(Point point) {
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

    Set<Point> getConvexPoints() {
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

    Set<Point> getConcavePoints() {
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
}
