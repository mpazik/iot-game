package dzida.server.core.world.pathfinding;

import dzida.server.core.basic.unit.BitMap;
import dzida.server.core.basic.unit.BitMap.ImmutableBitMap;
import dzida.server.core.basic.unit.Point;
import dzida.server.core.basic.unit.TreeNode;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static dzida.server.core.basic.unit.BitMap.createBitMap;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
public class BitMapTrackerTest {
    private static final BitMapTracker tracker = new BitMapTracker();

    @Test
    public void shouldReturnEmptySetForEmptyBitMap() {
        BitMap bitMap = ImmutableBitMap.builder(0, 0).build();

        List<TreeNode<Polygon>> polygons = tracker.track(bitMap);

        assertThat(polygons).isEmpty();
    }

    @Test
    public void shouldHandleElementarySquare() {
        BitMap bitMap = createBitMap(
                "   ",
                " # ",
                "   "
        );

        TreeNode<Polygon> expectedPolygon = createNodeWithPolygon(p(1, 1), p(2, 1), p(2, 2), p(1, 2));

        List<TreeNode<Polygon>> polygons = tracker.track(bitMap);

        assertThat(polygons).contains(expectedPolygon);
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

        TreeNode<Polygon> expectedPolygon = createNodeWithPolygon(p(1, 1), p(6, 1), p(6, 6), p(1, 6));

        List<TreeNode<Polygon>> polygons = tracker.track(bitMap);

        assertThat(polygons).contains(expectedPolygon);
    }

    @Test
    public void shouldHandleFilledBitMap() {
        BitMap bitMap = createBitMap(
                "##",
                "##"
        );

        TreeNode<Polygon> expectedPolygon = createNodeWithPolygon(p(0, 0), p(2, 0), p(2, 2), p(0, 2));

        List<TreeNode<Polygon>> polygons = tracker.track(bitMap);

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
        TreeNode<Polygon> expectedPolygon = createNodeWithPolygon(
                p(0, 0), p(2, 0),
                p(2, 1), p(1, 1),
                p(1, 2), p(2, 2),
                p(2, 5), p(0, 5),
                p(0, 4), p(1, 4),
                p(1, 3), p(0, 3)
        );

        List<TreeNode<Polygon>> polygons = tracker.track(bitMap);

        assertThat(polygons).contains(expectedPolygon);
    }

    @Test
    public void shouldFindMultiplePolygons() {
        BitMap bitMap = createBitMap(
                "## #",
                "## #",
                "    ",
                "####"
        );
        TreeNode<Polygon> polygon1 = createNodeWithPolygon(p(0, 0), p(2, 0), p(2, 2), p(0, 2));
        TreeNode<Polygon> polygon2 = createNodeWithPolygon(p(3, 0), p(4, 0), p(4, 2), p(3, 2));
        TreeNode<Polygon> polygon3 = createNodeWithPolygon(p(0, 3), p(4, 3), p(4, 4), p(0, 4));

        List<TreeNode<Polygon>> polygons = tracker.track(bitMap);

        assertThat(polygons).contains(polygon1, polygon2, polygon3);
    }

    @Test
    public void shouldFindPolygonWithPolygonInsideIt() {
        BitMap bitMap = createBitMap(
                "     ",
                " ### ",
                " # # ",
                " ### ",
                "     "
        );
        Polygon mainPolygon = createPolygon(p(1, 1), p(4, 1), p(4, 4), p(1, 4));
        TreeNode<Polygon> childPolygon = createNodeWithPolygon(p(2, 2), p(3, 2), p(3, 3), p(2, 3));
        TreeNode<Polygon> expectedPolygon = TreeNode.of(mainPolygon, childPolygon);

        List<TreeNode<Polygon>> polygons = tracker.track(bitMap);

        assertThat(polygons).contains(expectedPolygon);
    }

    @Test
    public void shouldFindPolygonWithWholesInConcavePointsAndItsChildren() {
        BitMap bitMap = createBitMap(
                " ### ",
                "# # #",
                "#####",
                "# # #",
                " ### "
        );
        Polygon mainPolygon = createPolygon(
                p(1, 0), p(4, 0), p(4, 1), p(5, 1),
                p(5, 4), p(4, 4), p(4, 5), p(1, 5),
                p(1, 4), p(0, 4), p(0, 1), p(1, 1));
        TreeNode<Polygon> expectedPolygon = TreeNode.of(mainPolygon,
                createNodeWithPolygon(p(1, 1), p(2, 1), p(2, 2), p(1, 2)),
                createNodeWithPolygon(p(3, 1), p(4, 1), p(4, 2), p(3, 2)),
                createNodeWithPolygon(p(1, 3), p(2, 3), p(2, 4), p(1, 4)),
                createNodeWithPolygon(p(3, 3), p(4, 3), p(4, 4), p(3, 4))
        );

        List<TreeNode<Polygon>> polygons = tracker.track(bitMap);

        assertThat(polygons).contains(expectedPolygon);
    }

    @Test
    public void shouldFindPolygonWithGrandGrandChildren() {
        BitMap bitMap = BitMap.createBitMap(
                "#######",
                "#     #",
                "# ### #",
                "# # # #",
                "# ### #",
                "#     #",
                "#######");
        Polygon grandGrandPolygon = createPolygon(p(0, 0), p(7, 0), p(7, 7), p(0, 7));
        Polygon grandPolygon = createPolygon(p(1, 1), p(6, 1), p(6, 6), p(1, 6));
        Polygon parentPolygon = createPolygon(p(2, 2), p(5, 2), p(5, 5), p(2, 5));
        Polygon childPolygon = createPolygon(p(3, 3), p(4, 3), p(4, 4), p(3, 4));
        TreeNode<Polygon> expectedPolygon = TreeNode.of(grandGrandPolygon,
                TreeNode.of(grandPolygon, TreeNode.of(parentPolygon, TreeNode.of(childPolygon)))
        );

        List<TreeNode<Polygon>> polygons = tracker.track(bitMap);

        assertThat(polygons).contains(expectedPolygon);
    }

    private Point p(int x, int y) {
        return Point.of(x, y);
    }

    private TreeNode<Polygon> createNodeWithPolygon(Point... points) {
        return TreeNode.of(createPolygon(points));
    }

    private Polygon createPolygon(Point... points) {
        return new Polygon(newArrayList(points));
    }

}