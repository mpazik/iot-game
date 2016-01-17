package dzida.server.core.basic.unit;

import static dzida.server.core.basic.unit.Points.isBetween;

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
        return isBetween(point.getX(), start.getX(), end.getX()) &&
                isBetween(point.getY(), start.getY(), end.getY()) &&
                lineVector().crossProduct(point.minus(start)) == 0.0;
    }

    //it's based on http://martin-thoma.com/how-to-check-if-two-line-segments-intersect/
    public boolean isIntersecting(Line line) {
        return crosses(line) &&
                line.crosses(this);
    }

    public Point getStart() {
        return start;
    }

    public Point getEnd() {
        return end;
    }

    //line argument is treated like infinite line, not like segment line
    private boolean crosses(Line line) {
        Point lineVector = lineVector();
        double crossProduct1 = lineVector.crossProduct(line.start.minus(start));
        double crossProduct2 = lineVector.crossProduct(line.end.minus(start));
        return crossProduct1 != 0 && crossProduct2 != 0 && crossProduct1 < 0 != crossProduct2 < 0;
    }
}
