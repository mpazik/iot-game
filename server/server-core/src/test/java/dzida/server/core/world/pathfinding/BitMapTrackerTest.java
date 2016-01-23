package dzida.server.core.world.pathfinding;

import dzida.server.core.basic.unit.BitMap;
import dzida.server.core.basic.unit.BitMap.ImmutableBitMap;
import dzida.server.core.basic.unit.PointList;
import dzida.server.core.basic.unit.TreeNode;
import org.junit.Test;

import java.util.List;

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

        TreeNode<Polygon> expectedPolygon = createNodeWithPolygon(1, 1, 2, 1, 2, 2, 1, 2);

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

        TreeNode<Polygon> expectedPolygon = createNodeWithPolygon(1, 1, 6, 1, 6, 6, 1, 6);

        List<TreeNode<Polygon>> polygons = tracker.track(bitMap);

        assertThat(polygons).contains(expectedPolygon);
    }

    @Test
    public void shouldHandleFilledBitMap() {
        BitMap bitMap = createBitMap(
                "##",
                "##"
        );

        TreeNode<Polygon> expectedPolygon = createNodeWithPolygon(0, 0, 2, 0, 2, 2, 0, 2);

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
                0, 0, 2, 0,
                2, 1, 1, 1,
                1, 2, 2, 2,
                2, 5, 0, 5,
                0, 4, 1, 4,
                1, 3, 0, 3
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
        TreeNode<Polygon> polygon1 = createNodeWithPolygon(0, 0, 2, 0, 2, 2, 0, 2);
        TreeNode<Polygon> polygon2 = createNodeWithPolygon(3, 0, 4, 0, 4, 2, 3, 2);
        TreeNode<Polygon> polygon3 = createNodeWithPolygon(0, 3, 4, 3, 4, 4, 0, 4);

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
        Polygon mainPolygon = createPolygon(1, 1, 4, 1, 4, 4, 1, 4);
        TreeNode<Polygon> childPolygon = createNodeWithPolygon(2, 2, 3, 2, 3, 3, 2, 3);
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
                1, 0, 4, 0, 4, 1, 5, 1,
                5, 4, 4, 4, 4, 5, 1, 5,
                1, 4, 0, 4, 0, 1, 1, 1);
        TreeNode<Polygon> expectedPolygon = TreeNode.of(mainPolygon,
                createNodeWithPolygon(1, 1, 2, 1, 2, 2, 1, 2),
                createNodeWithPolygon(3, 1, 4, 1, 4, 2, 3, 2),
                createNodeWithPolygon(1, 3, 2, 3, 2, 4, 1, 4),
                createNodeWithPolygon(3, 3, 4, 3, 4, 4, 3, 4)
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
        Polygon grandGrandPolygon = createPolygon(0, 0, 7, 0, 7, 7, 0, 7);
        Polygon grandPolygon = createPolygon(1, 1, 6, 1, 6, 6, 1, 6);
        Polygon parentPolygon = createPolygon(2, 2, 5, 2, 5, 5, 2, 5);
        Polygon childPolygon = createPolygon(3, 3, 4, 3, 4, 4, 3, 4);
        TreeNode<Polygon> expectedPolygon = TreeNode.of(grandGrandPolygon,
                TreeNode.of(grandPolygon, TreeNode.of(parentPolygon, TreeNode.of(childPolygon)))
        );

        List<TreeNode<Polygon>> polygons = tracker.track(bitMap);

        assertThat(polygons).contains(expectedPolygon);
    }

    private TreeNode<Polygon> createNodeWithPolygon(double ...points) {
        return TreeNode.of(createPolygon(points));
    }

    private Polygon createPolygon(double ...points) {
        return new Polygon(PointList.builder().add(points).build());
    }

}