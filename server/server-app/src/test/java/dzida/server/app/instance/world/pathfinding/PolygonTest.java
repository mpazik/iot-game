package dzida.server.app.instance.world.pathfinding;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import dzida.server.app.basic.unit.Point;
import dzida.server.app.basic.unit.PointList;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(HierarchicalContextRunner.class)
public class PolygonTest {

    private final Polygon polygon = createPolygon();

    @Test
    public void convexPointsAreReturned() {

        List<Point> convexPoints = polygon.getConvexPoints();
        assertThat(convexPoints).contains(p(1, 0), p(6, 0), p(6, 3), p(3, 3), p(2, 3), p(1, 3), p(0, 2), p(0, 1));
    }

    @Test
    public void concavePointsAreReturned() {

        List<Point> concavePoints = polygon.getConcavePoints();
        assertThat(concavePoints).contains(p(3, 1), p(2, 1), p(1, 2), p(1, 1));
    }

    @Test
    public void isOnBorder() {
        // middle line
        assertThat(polygon.isOnBorder(p(3, 0))).isTrue();
        // vertex
        assertThat(polygon.isOnBorder(p(1, 0))).isTrue();
        // middle of last
        assertThat(polygon.isOnBorder(p(1, 0.5))).isTrue();

        // inside of polygon
        assertThat(polygon.isOnBorder(p(1.5, 0.5))).isFalse();
        // outside of polygon
        assertThat(polygon.isOnBorder(p(0.5, 0.5))).isFalse();
    }

    @Test
    public void returnsAllIntersectionWithBoard() {
        // single point
        assertThat(polygon.getIntersections(0.5, 0.5, 3, 0.5).toList()).containsExactly(p(1, 0.5));

        // multiple points
        assertThat(polygon.getIntersections(-1.0, 1.5, 8, 1.5).toList()).containsExactly(p(6, 1.5), p(3, 1.5), p(2, 1.5), p(0, 1.5));

        // empty if point is on border
        assertThat(polygon.getIntersections(1.0, 0.5, 6, 0.5).toList()).isEmpty();

        // empty on no intersection
        assertThat(polygon.getIntersections(0.0, 0.5, 0.5, 0.5).toList()).isEmpty();
    }

    /**
     * This method will create a polygon this shape like this:
     * .#####
     * ##.###
     * .#.###
     */
    private Polygon createPolygon() {
        PointList points = PointList.builder().add(
                1, 0, 6, 0,
                6, 3, 3, 3,
                3, 1, 2, 1,
                2, 3, 1, 3,
                1, 2, 0, 2,
                0, 1, 1, 1
        ).build();
        return new Polygon(points);
    }

    private Point p(double x, double y) {
        return Point.of(x, y);
    }

    public class IsInside {
        @Test
        public void pointIsInsidePolygon_isTrue() {
            assertThat(polygon.isInside(5, 0.5)).isTrue();
            assertThat(polygon.isInside(5, 1)).isTrue();
            assertThat(polygon.isInside(1.1, 2)).isTrue();
        }

        @Test
        public void pointIsOutSidePolygon_isFalse() {
            assertThat(polygon.isInside(2, 1)).isFalse();
            assertThat(polygon.isInside(5, 3.5)).isFalse();
            assertThat(polygon.isInside(5, -1)).isFalse();
        }
    }

    public class IsLineOutside {
        @Test
        public void lineIsOutside_isTrue() {
            assertThat(polygon.isLineOutside(2.5, 1.5, 2.5, 3)).isTrue();
        }

        @Test
        public void lineTouchBorderFromOutside_isTrue() {
            assertThat(polygon.isLineOutside(0, 0,1, 0.5)).isTrue();
            assertThat(polygon.isLineOutside(0, 0, 1, 1)).isTrue();
            assertThat(polygon.isLineOutside(2.5, 1.5,  2, 1.5)).isTrue();
        }

        @Test
        public void lineCrossPolygon_isFalse() {
            assertThat(polygon.isLineOutside(0, 0.5, 1.5, 0.5)).isFalse();
            assertThat(polygon.isLineOutside(2.5, 1.5, 4, 1.5)).isFalse();
            assertThat(polygon.isLineOutside(0, 0, 1.5, 1.5)).isFalse();
            assertThat(polygon.isLineOutside(6, 0, 2, 3)).isFalse();
        }

        @Test
        public void lineIsInside_isFalse() {
            assertThat(polygon.isLineOutside(1.5, 0.5, 3, 0.5)).isFalse();
        }

        @Test
        public void lineTouchBorderFromInside_isFalse() {
            assertThat(polygon.isLineOutside(1, 0.5, 2, 0.5)).isFalse();
            assertThat(polygon.isLineOutside(1, 1.5, 0, 1.5)).isFalse();

            // touch from two vertexes
            assertThat(polygon.isLineOutside(0, 2, 2, 2)).isFalse();

            // touch vertex and line
            assertThat(polygon.isLineOutside(0, 2, 2, 2)).isFalse();
        }

        @Test
        public void lineIsOnBorder_isTrue() {
            // line to line
            assertThat(polygon.isLineOutside(1, 0, 6, 0)).isTrue();
            // point on vertex
            assertThat(polygon.isLineOutside(1, 1, 1, 1)).isTrue();
            // point on line
            assertThat(polygon.isLineOutside(1, 0.5, 1, 0.5)).isTrue();
        }
    }

    public class IsLineInside {
        @Test
        public void lineIsInside_isTrue() {
            assertThat(polygon.isLineInside(1.5, 0.5, 3, 0.5)).isTrue();
        }

        @Test
        public void lineTouchBorderFromInside_isTrue() {
            assertThat(polygon.isLineInside(1, 0.5, 2, 0.5)).isTrue();
            assertThat(polygon.isLineInside(1, 1.5, 0, 1.5)).isTrue();

            // point on vertex
            assertThat(polygon.isLineInside(1, 1, 1, 1)).isTrue();
            // point on line
            assertThat(polygon.isLineInside(1, 0.5, 1, 0.5)).isTrue();
        }

        @Test
        public void lineCrossPolygon_isFalse() {
            assertThat(polygon.isLineInside(0, 0.5, 1.5, 0.5)).isFalse();
            assertThat(polygon.isLineInside(2.5, 1.5, 4, 1.5)).isFalse();
            assertThat(polygon.isLineInside(0, 0, 1.5, 1.5)).isFalse();
            assertThat(polygon.isLineInside(6, 0, 2, 3)).isFalse();
        }

        @Test
        public void lineIsOutside_isFalse() {
            assertThat(polygon.isLineInside(2.5, 1.5, 2.5, 3)).isFalse();
        }

        @Test
        public void lineTouchBorderFromOutside_isFalse() {
            assertThat(polygon.isLineInside(0, 0, 1, 0.5)).isFalse();
            assertThat(polygon.isLineInside(0, 0, 1, 1)).isFalse();
            assertThat(polygon.isLineInside(2.5, 1.5, 2, 1.5)).isFalse();

            // touch from two vertexes
            assertThat(polygon.isLineInside(2, 1, 3, 3)).isFalse();

            // touch vertex and line
            assertThat(polygon.isLineInside(2, 1, 3, 1.5)).isFalse();
        }
    }
}