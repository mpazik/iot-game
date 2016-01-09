package dzida.server.core.basic.unit;

import java.util.Objects;

public final class Point {
    private final double x;
    private final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Point of(double x, double y) {
        return new Point(x, y);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Point plus(Point position) {
        return new Point(this.x + position.x, this.y + position.y);
    }

    public Point minus(Point position) {
        return new Point(this.x - position.x, this.y - position.y);
    }

    public Point multiply(double scalar) {
        return new Point(this.x * scalar, this.y * scalar);
    }

    public Point divide(double scalar) {
        return new Point(this.x / scalar, this.y / scalar);
    }

    public double distanceTo(Point position) {
        return Math.sqrt(this.distanceSqrTo(position));
    }

    public double distanceSqrTo(Point position) {
        double dx = position.x - this.x;
        double dy = position.y - this.y;
        return dx * dx + dy * dy;
    }

    public boolean isInRange(Point position, double radius) {
        return distanceSqrTo(position) <= radius * radius;
    }

    public double crossProduct(Point p) {
        return x * p.y - y * p.y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point position = (Point) o;
        return Double.compare(position.x, x) == 0 &&
                Double.compare(position.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ')';
    }
}
