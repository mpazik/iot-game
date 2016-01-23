package dzida.server.core.basic.unit;

import java.util.Optional;

public class Line {
    private final Point start;
    private final Point end;

    public Line(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

    public static Line of(Point p1, Point p2) {
        return new Line(p1, p2);
    }

    public static Line of(double x1, double y1, double x2, double y2) {
        return new Line(Point.of(x1, y1), Point.of(x2, y2));
    }

    public double length() {
        return start.distanceTo(end);
    }

    public double lengthSqr() {
        return start.distanceSqrTo(end);
    }

    public Point lineVector() {
        return end.minus(start);
    }

    public Point interpolate(double ratio) {
        return start.plus(lineVector().multiply(ratio));
    }

    public boolean isPointOnLine(Point point) {
        return Geometry2D.isPointOnLine(point.getX(), point.getY(), start.getX(), start.getY(), end.getX(), end.getY());
    }

    public boolean isIntersecting(Line line) {
        return Geometry2D.isIntersecting(start.getX(), start.getY(), end.getX(), end.getY(),
                line.getStart().getX(), line.getStart().getY(), line.getEnd().getX(), line.getEnd().getY());
    }

    public Optional<Point> getIntersection(Line line) {
        return Geometry2D.getIntersection(start.getX(), start.getY(), end.getX(), end.getY(),
                line.getStart().getX(), line.getStart().getY(), line.getEnd().getX(), line.getEnd().getY());
    }

    public Point getStart() {
        return start;
    }

    public Point getEnd() {
        return end;
    }
}
