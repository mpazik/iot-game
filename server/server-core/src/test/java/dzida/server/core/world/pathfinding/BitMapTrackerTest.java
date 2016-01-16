package dzida.server.core.world.pathfinding;

import dzida.server.core.basic.unit.BitMap;
import dzida.server.core.basic.unit.Point;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;

public class BitMapTrackerTest {
    private static final BitMapTracker tracker = new BitMapTracker();

    @Test
    public void shouldReturnEmptySetForEmptyBitMap() {
        BitMap bitMap = BitMap.builder(0, 0).build();

        Set<Polygon> polygons = tracker.track(bitMap);

        Assertions.assertThat(polygons).isEmpty();
    }

    @Test
    public void shouldHandleElementarySquare() {
        BitMap bitMap = createBitMap(
                "   ",
                " # ",
                "   "
        );

        List<Point> points = newArrayList(p(1, 1), p(2, 1), p(2, 2), p(1, 2));
        Polygon expectedPolygon = new Polygon(points, createBitMap("#"), 1, 1, emptyList(), points);

        Set<Polygon> polygons = tracker.track(bitMap);

        Assertions.assertThat(polygons).contains(expectedPolygon);
    }

    @Test
    public void shouldHandleSquare() {
        BitMap bitMap = createBitMap(
                "       ",
                " ##### ",
                " ##### ",
                " ##### ",
                " ##### ",
                " ##### ",
                "       "
        );

        List<Point> points = newArrayList(p(1, 1), p(6, 1), p(6, 6), p(1, 6));
        BitMap polygonMap = createBitMap("#####", "#####", "#####", "#####", "#####");
        Polygon expectedPolygon = new Polygon(points, polygonMap, 1, 1, emptyList(), points);

        Set<Polygon> polygons = tracker.track(bitMap);

        Assertions.assertThat(polygons).contains(expectedPolygon);
    }

    @Test
    public void shouldHandleFilledBitMap() {
        BitMap bitMap = createBitMap(
                "##",
                "##"
        );

        List<Point> points = newArrayList(p(0, 0), p(2, 0), p(2, 2), p(0, 2));
        Polygon expectedPolygon = new Polygon(points, bitMap, 0, 0, emptyList(), points);

        Set<Polygon> polygons = tracker.track(bitMap);

        Assertions.assertThat(polygons).contains(expectedPolygon);
    }

    @Test
    public void shouldHandleConcavePoints() {
        BitMap bitMap = createBitMap(
                "##",
                "# ",
                "##",
                " #",
                "##"
        );
        List<Point> points = newArrayList(
                p(0, 0), p(2, 0),
                p(2, 1), p(1, 1),
                p(1, 2), p(2, 2),
                p(2, 5), p(0, 5),
                p(0, 4), p(1, 4),
                p(1, 3), p(0, 3)
        );
        List<Point> concavePoints = newArrayList(p(1, 1), p(1, 2), p(1, 4), p(1, 3));
        List<Point> convexPoints = newArrayList(p(0, 0), p(2, 0), p(2, 1), p(2, 2), p(2, 5), p(0, 5), p(0, 4), p(0, 3));
        Polygon expectedPolygon = new Polygon(points, bitMap, 0, 0, concavePoints, convexPoints);

        Set<Polygon> polygons = tracker.track(bitMap);

        Assertions.assertThat(polygons).contains(expectedPolygon);
    }

    @Test
    public void shouldFindMultiplePolygons() {
        BitMap bitMap = createBitMap(
                "## #",
                "## #",
                "    ",
                "####"
        );
        List<Point> points1 = newArrayList(p(0, 0), p(2, 0), p(2, 2), p(0, 2));
        Polygon polygon1 = new Polygon(points1, createBitMap("##", "##"), 0, 0, emptyList(), points1);
        List<Point> points2 = newArrayList(p(3, 0), p(4, 0), p(4, 2), p(3, 2));
        Polygon polygon2 = new Polygon(points2, createBitMap("#", "#"), 3, 0, emptyList(), points2);
        List<Point> points3 = newArrayList(p(0, 3), p(4, 3), p(4, 4), p(0, 4));
        Polygon polygon3 = new Polygon(points3, createBitMap("####"), 0, 3, emptyList(), points3);

        Set<Polygon> polygons = tracker.track(bitMap);

        Assertions.assertThat(polygons).contains(polygon1, polygon2, polygon3);
    }

    private Point p(int x, int y) {
        return Point.of(x, y);
    }

    private BitMap createBitMap(String... rows) {
        int height = rows.length;
        int width = height == 0 ? 0 : rows[0].length();

        BitMap.Builder builder = BitMap.builder(width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char c = rows[y].charAt(x);
                builder.set(x, y, c == '#');
            }
        }

        return builder.build();
    }


}