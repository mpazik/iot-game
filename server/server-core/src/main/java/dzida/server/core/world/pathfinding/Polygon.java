package dzida.server.core.world.pathfinding;

import com.google.common.base.Objects;
import dzida.server.core.basic.unit.BitMap;
import dzida.server.core.basic.unit.Line;
import dzida.server.core.basic.unit.Point;

import java.util.List;

class Polygon {
    private final List<Point> points;
    private final BitMap polygonMap;
    private final int x;
    private final int y;
    private final List<Point> concavePoints;
    private final List<Point> convexPoints;

    Polygon(List<Point> points, BitMap polygonMap, int x, int y, List<Point> concavePoints, List<Point> convexPoints) {
        this.points = points;
        this.polygonMap = polygonMap;
        this.x = x;
        this.y = y;
        this.concavePoints = concavePoints;
        this.convexPoints = convexPoints;
    }

    public boolean intersect(Line line) {
        for (int i = 1; i < points.size(); i++) {
            Point p1 = points.get(i - 1);
            Point p2 = points.get(i);
            if (line.isIntersecting(new Line(p1, p2))) {
                return true;
            }
        }
        return line.isIntersecting(new Line(points.get(0), points.get(points.size() - 1)));
    }

    boolean isInside(int x, int y) {
        int relativeX = x - this.x;
        int relativeY = y - this.y;
        return polygonMap.isSet(relativeX, relativeY);
    }

    List<Point> getConvexPoints() {
        return convexPoints;
    }

    List<Point> getConcavePoints() {
        return concavePoints;
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

    @Override
    public String toString() {
        return "Polygon{" +
                "points=" + points +
                '}';
    }

    public BitMap getPolygonMap() {
        return polygonMap;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
