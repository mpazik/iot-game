package dzida.server.core.basic.geometry;

import dzida.server.core.basic.unit.BitMap;
import dzida.server.core.basic.unit.Point;
import dzida.server.core.basic.unit.Polygon;
import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class BitMapTrackerTest {
    private static final BitMapTracker geometry = new BitMapTracker();

    @Test
    public void shouldReturnEmptySetForEmptyBitMap() {
        BitMap bitMap = BitMap.builder(0, 0).build();

        Set<Polygon> polygons = geometry.track(bitMap);

        assertThat(polygons).isEmpty();
    }

    @Test
    public void shouldHandleElementarySquare() {
        BitMap bitMap = createBitMap(
                "   ",
                " # ",
                "   "
        );
        Polygon expectedPolygon = Polygon.of(p(1, 1), p(2, 1), p(2, 2), p(1, 2));

        Set<Polygon> polygons = geometry.track(bitMap);

        assertThat(polygons).contains(expectedPolygon);
    }

    @Test
    public void shouldHandleSquare() {
        BitMap bitMap = createBitMap(
                "     ",
                " ### ",
                " ### ",
                " ### ",
                "     "
        );
        Polygon expectedPolygon = Polygon.of(p(1, 1), p(4, 1), p(4, 4), p(1, 4));

        Set<Polygon> polygons = geometry.track(bitMap);

        assertThat(polygons).contains(expectedPolygon);
    }

    @Test
    public void shouldHandleFilledBitMap() {
        BitMap bitMap = createBitMap(
                "##",
                "##"
        );
        Polygon expectedPolygon = Polygon.of(p(0, 0), p(2, 0), p(2, 2), p(0, 2));

        Set<Polygon> polygons = geometry.track(bitMap);

        assertThat(polygons).contains(expectedPolygon);
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
        Polygon expectedPolygon = Polygon.of(
                p(0, 0), p(2, 0),
                p(2, 1), p(1, 1),
                p(1, 2), p(2, 2),
                p(2, 5), p(0, 5),
                p(0, 4), p(1, 4),
                p(1, 3), p(0, 3)
        );

        Set<Polygon> polygons = geometry.track(bitMap);

        assertThat(polygons).contains(expectedPolygon);
    }

    private Point p(int x, int y) {
        return Point.of(x, y);
    }

    private BitMap createBitMap(String ...rows) {
        int height = rows.length;
        int width = height == 0 ? 0 : rows[0].length();

        BitMap.Builder builder = BitMap.builder(width, height);
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                char c = rows[y].charAt(x);
                builder.set(x, y, c == '#');
            }
        }

        return builder.build();
    }


}