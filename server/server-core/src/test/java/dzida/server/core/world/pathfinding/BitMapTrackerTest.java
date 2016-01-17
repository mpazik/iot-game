package dzida.server.core.world.pathfinding;

import dzida.server.core.basic.unit.BitMap;
import dzida.server.core.basic.unit.BitMap.ImmutableBitMap;
import dzida.server.core.basic.unit.Point;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static dzida.server.core.basic.unit.BitMap.createBitMap;

public class BitMapTrackerTest {
    private static final BitMapTracker tracker = new BitMapTracker();

    @Test
    public void shouldReturnEmptySetForEmptyBitMap() {
        BitMap bitMap = ImmutableBitMap.builder(0, 0).build();

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

        Polygon expectedPolygon = new Polygon(newArrayList(p(1, 1), p(2, 1), p(2, 2), p(1, 2)));

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

        Polygon expectedPolygon = new Polygon(newArrayList(p(1, 1), p(6, 1), p(6, 6), p(1, 6)));

        Set<Polygon> polygons = tracker.track(bitMap);

        Assertions.assertThat(polygons).contains(expectedPolygon);
    }

    @Test
    public void shouldHandleFilledBitMap() {
        BitMap bitMap = createBitMap(
                "##",
                "##"
        );

        Polygon expectedPolygon = new Polygon(newArrayList(p(0, 0), p(2, 0), p(2, 2), p(0, 2)));

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
        Polygon expectedPolygon = new Polygon(newArrayList(
                p(0, 0), p(2, 0),
                p(2, 1), p(1, 1),
                p(1, 2), p(2, 2),
                p(2, 5), p(0, 5),
                p(0, 4), p(1, 4),
                p(1, 3), p(0, 3)
        ));

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
        Polygon polygon1 = new Polygon(points1);
        Polygon polygon2 = new Polygon(newArrayList(p(3, 0), p(4, 0), p(4, 2), p(3, 2)));
        Polygon polygon3 = new Polygon(newArrayList(p(0, 3), p(4, 3), p(4, 4), p(0, 4)));

        Set<Polygon> polygons = tracker.track(bitMap);

        Assertions.assertThat(polygons).contains(polygon1, polygon2, polygon3);
    }

    @Test
    public void shouldFindPolygonWithWholesInConcavePoints() {
        BitMap bitMap = createBitMap(
                " ### ",
                "# # #",
                "#####",
                "# # #",
                " ### "
        );
        List<Point> points1 = newArrayList(
                p(1, 0), p(4, 0), p(4, 1), p(5, 1),
                p(5, 4), p(4, 4), p(4, 5), p(1, 5),
                p(1, 4), p(0, 4), p(0, 1), p(1, 1));
        Polygon expectedPolygon = new Polygon(points1);

        Set<Polygon> polygons = tracker.track(bitMap);

        Assertions.assertThat(polygons).contains(expectedPolygon);
    }

    private Point p(int x, int y) {
        return Point.of(x, y);
    }

}