package dzida.server.core.basic.unit;

public class Points {
    private Points() {
        //no instance
    }

    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(distanceSqr(x1, y1, x2, y2));
    }

    public static double distanceSqr(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return dx * dx + dy * dy;
    }

    public static double getLength(double[] points) {
        double length = 0;
        if (isSinglePoint(points)) {
            return 0;
        }
        for (int i = 0; i < points.length - 2; i += 2) {
            length += Points.distance(points[i], points[i + 1], points[i + 2], points[i + 3]);
        }
        return length;
    }

    public static boolean isSinglePoint(double[] points) {
        return points.length == 2;
    }

    @FunctionalInterface
    public interface IntPointOperator {
        void apply(int x, int y);
    }
}
