package dzida.server.app.basic.unit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.System.arraycopy;

public final class PointList {
    /**
     * Points represented as array of doubles when x'es are even and y'es are odd.
     */
    private final double[] points;

    private PointList(double[] points) {
        this.points = points;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(PointList pointList) {
        return new Builder(pointList);
    }

    public double x(int pointIndex) {
        return this.points[pointIndex * 2];
    }

    public double y(int pointIndex) {
        return this.points[pointIndex * 2 + 1];
    }

    public Point getPoint(int i) {
        return new Point(x(i), y(i));
    }

    public List<Point> toList() {
        List<Point> points = new ArrayList<>(size());
        for (int i=0; i<size(); i++) {
            points.add(new Point(x(i), y(i)));
        }
        return points;
    }

    public int size() {
        return points.length/2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PointList pointList = (PointList) o;
        return Arrays.equals(points, pointList.points);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(points);
    }

    @Override
    public String toString() {
        return "PointList{" +
                "points=" + Arrays.toString(points) +
                '}';
    }

    public int indexOf(Point node) {
        for (int i=0; i<size(); i++) {
            if (x(i) == node.getX() && y(i) == node.getY()) {
                return i;
            }
        }
        return -1;
    }

    public static class Builder {
        private double[] points;
        private int size;


        private Builder(PointList pointList) {
            size = pointList.points.length;
            points = Arrays.copyOf(pointList.points, size + 20);
        }

        private Builder() {
            size = 0;
            points = new double[20];
        }

        public Builder add(double x, double y) {
            ensureExplicitCapacity(size + 2);
            points[size] = x;
            points[size+1] = y;
            size += 2;
            return this;
        }

        public Builder add(Point point) {
            return add(point.getX(), point.getY());
        }

        public Builder add(double ...cords) {
            if (cords.length % 2 != 0) {
                throw new RuntimeException("Number of cords have to be even");
            }
            ensureExplicitCapacity(size + cords.length);
            arraycopy(cords, 0, points, size, cords.length);
            size += cords.length;

            return this;
        }

        public Builder add(PointList pointList) {
            add(pointList.points);
            return this;
        }

        public PointList build() {
            return new PointList(Arrays.copyOf(points, size));
        }

        private void ensureExplicitCapacity(int minCapacity) {
            if (minCapacity - points.length > 0)
                grow(minCapacity);
        }

        private void grow(int minCapacity) {
            int oldCapacity = size;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0)
                newCapacity = minCapacity;
            points = Arrays.copyOf(points, newCapacity);
        }
    }
}
