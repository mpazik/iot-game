package dzida.server.core.world.pathfinding;

import com.google.common.collect.ImmutableSet;
import dzida.server.core.basic.unit.Line;
import dzida.server.core.basic.unit.Point;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

public class PolygonTest {

    private final Polygon polygon = createPolygon();

    @Test
    public void convexPointsAreReturned() {
        Set<Point> expectedConvexPoints = ImmutableSet.of(p(1, 0), p(6, 0), p(6, 3), p(3, 3), p(2, 3), p(1, 3), p(0, 2), p(0, 1));

        Set<Point> convexPoints = polygon.getConvexPoints();
        assertThat(convexPoints).isEqualTo(expectedConvexPoints);
    }

    @Test
    public void concavePointsAreReturned() {
        Set<Point> expectedConcavePoints = ImmutableSet.of(p(3, 1), p(2, 1), p(1, 2), p(1, 1));

        Set<Point> concavePoints = polygon.getConcavePoints();
        assertThat(concavePoints).isEqualTo(expectedConcavePoints);
    }

    @Test
    public void pointIsInsidePolygon() {
        assertThat(polygon.isInside(5, 1)).isTrue();
    }

    @Test
    public void pointIsInsidePolygonIfIsOnLeftLine() {
        assertThat(polygon.isInside(0, 1)).isTrue();
    }

    @Test
    public void pointIsNotInSidePolygon() {
        assertThat(polygon.isInside(2, 1)).isFalse();
    }

    @Test
    public void pointIsNotInsidePolygonIfIsOnRightLine() {
        assertThat(polygon.isInside(6, 1)).isFalse();
    }

    @Test
    public void intersection() {
        assertThat(polygon.intersect(new Line(p(0, 0.5), p(1.5, 0.5)))).isTrue();
        assertThat(polygon.intersect(new Line(p(2.5, 1.5), p(4, 1.5)))).isTrue();
        assertThat(polygon.intersect(new Line(p(0, 0), p(1.5, 1.5)))).isTrue();
        assertThat(polygon.intersect(new Line(p(6, 0), p(2, 3)))).isTrue();
    }

    @Test
    public void noIntersectionIfLineIsOutsideOfPolygon() {
        assertThat(polygon.intersect(new Line(p(2.5, 1.5), p(2.5, 3)))).isFalse();
    }

    @Test
    public void intersectionIfLineIsInsideOfPolygon() {
        assertThat(polygon.intersect(new Line(p(1.5, 0.5), p(3, 0.5)))).isTrue() ;
    }

    @Test
    public void noIntersectionIfLineTouchBorderFromOutside() {
        assertThat(polygon.intersect(new Line(p(0, 0), p(1, 0.5)))).isFalse();
        assertThat(polygon.intersect(new Line(p(0, 0), p(1, 1)))).isFalse();
        assertThat(polygon.intersect(new Line(p(2.5, 1.5), p(2, 1.5)))).isFalse();
    }

    @Test
    public void noIntersectionIfLineIsOnBorder() {
        // line to line
        assertThat(polygon.intersect(new Line(p(1, 0), p(6, 0)))).isFalse();
        // point on vertex
        assertThat(polygon.intersect(new Line(p(1, 1), p(1, 1)))).isFalse();
        // point on line
        assertThat(polygon.intersect(new Line(p(1, 0.5), p(1, 0.5)))).isFalse();
    }

    @Test
    public void intersectionIfLineTouchBorderFromInside() {
        assertThat(polygon.intersect(new Line(p(1, 0.5), p(2, 0.5)))).isTrue();
        assertThat(polygon.intersect(new Line(p(1, 1.5), p(0, 1.5)))).isTrue();

        // touch from two vertexes
        assertThat(polygon.intersect(new Line(p(0, 2), p(2, 2)))).isTrue();

        // touch vertex and line
        assertThat(polygon.intersect(new Line(p(0, 2), p(2, 2)))).isTrue();
    }

    @Test
    public void intersectionInside() {
        assertThat(polygon.intersectInside(new Line(p(0, 0.5), p(1.5, 0.5)))).isTrue();
        assertThat(polygon.intersectInside(new Line(p(2.5, 1.5), p(4, 1.5)))).isTrue();
        assertThat(polygon.intersectInside(new Line(p(0, 0), p(1.5, 1.5)))).isTrue();
        assertThat(polygon.intersectInside(new Line(p(6, 0), p(2, 3)))).isTrue();
    }

    @Test
    public void intersectionInsideIfLineIsOutsideOfPolygon() {
        assertThat(polygon.intersectInside(new Line(p(2.5, 1.5), p(2.5, 3)))).isTrue();
    }

    @Test
    public void noIntersectionInsideIfLineIsInsideOfPolygon() {
        assertThat(polygon.intersectInside(new Line(p(1.5, 0.5), p(3, 0.5)))).isFalse() ;
    }

    @Test
    public void intersectionInsideIfLineTouchBorderFromOutside() {
        assertThat(polygon.intersectInside(new Line(p(0, 0), p(1, 0.5)))).isTrue();
        assertThat(polygon.intersectInside(new Line(p(0, 0), p(1, 1)))).isTrue();
        assertThat(polygon.intersectInside(new Line(p(2.5, 1.5), p(2, 1.5)))).isTrue();

        // touch from two vertexes
        assertThat(polygon.intersectInside(new Line(p(2, 1), p(3, 3)))).isTrue();

        // touch vertex and line
        assertThat(polygon.intersectInside(new Line(p(2, 1), p(3, 1.5)))).isTrue();
    }

    @Test
    public void noIntersectionInsideIfLineTouchBorderFromInside() {
        assertThat(polygon.intersectInside(new Line(p(1, 0.5), p(2, 0.5)))).isFalse();
        assertThat(polygon.intersectInside(new Line(p(1, 1.5), p(0, 1.5)))).isFalse();

        // point on vertex
        assertThat(polygon.intersectInside(new Line(p(1, 1), p(1, 1)))).isFalse();
        // point on line
        assertThat(polygon.intersectInside(new Line(p(1, 0.5), p(1, 0.5)))).isFalse();
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
        assertThat(polygon.getIntersections(Line.of(0.5, 0.5, 3, 0.5))).containsExactly(p(1, 0.5));

        // multiple points
        assertThat(polygon.getIntersections(Line.of(-1.0, 1.5, 8, 1.5))).containsExactly(p(6, 1.5), p(3, 1.5), p(2, 1.5), p(0, 1.5));

        // boarders included
        assertThat(polygon.getIntersections(Line.of(1.0, 0.5, 6, 0.5))).containsExactly(p(1, 0.5), p(6, 0.5));

        // empty on no intersection
        assertThat(polygon.getIntersections(Line.of(0.0, 0.5, 0.5, 0.5))).isEmpty();
    }

    /**
     * This method will create a polygon this shape like this:
     * .#####
     * ##.###
     * .#.###
     */
    private Polygon createPolygon() {
        List<Point> points = newArrayList(
                p(1, 0), p(6, 0),
                p(6, 3), p(3, 3),
                p(3, 1), p(2, 1),
                p(2, 3), p(1, 3),
                p(1, 2), p(0, 2),
                p(0, 1), p(1, 1)
        );
        return new Polygon(points);
    }

    private Point p(double x, double y) {
        return Point.of(x, y);
    }
}