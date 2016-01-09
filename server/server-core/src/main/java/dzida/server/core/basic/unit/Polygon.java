package dzida.server.core.basic.unit;

import com.google.common.base.Objects;

import java.util.Arrays;
import java.util.List;

public class Polygon {
    private final List<Point> points;

    public Polygon(List<Point> points) {
        this.points = points;
    }

    public static Polygon of(Point... points) {
        return new Polygon(Arrays.asList(points));
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
}
