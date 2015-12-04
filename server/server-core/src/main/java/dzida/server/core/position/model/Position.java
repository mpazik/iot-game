package dzida.server.core.position.model;

import java.util.Objects;

public final class Position {
    private final double x;
    private final double y;

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Position of(double x, double y) {
        return new Position(x, y);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Position plus(Position position) {
        return new Position(this.x + position.x, this.y + position.y);
    }

    public Position minus(Position position) {
        return new Position(this.x - position.x, this.y - position.y);
    }

    public Position multiply(double scalar) {
        return new Position(this.x * scalar, this.y * scalar);
    }

    public Position divide(double scalar) {
        return new Position(this.x / scalar, this.y / scalar);
    }

    public double distanceTo(Position position) {
        return Math.sqrt(this.distanceSqrTo(position));
    }

    public double distanceSqrTo(Position position) {
        double dx = position.x - this.x;
        double dy = position.y - this.y;
        return dx * dx + dy * dy;
    }

    public boolean isInRange(Position position, double radius) {
        return distanceSqrTo(position) <= radius * radius;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return Double.compare(position.x, x) == 0 &&
                Double.compare(position.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
