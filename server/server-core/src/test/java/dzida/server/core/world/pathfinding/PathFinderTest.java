package dzida.server.core.world.pathfinding;

import dzida.server.core.basic.unit.BitMap;
import dzida.server.core.basic.unit.Point;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PathFinderTest {

    private final PathFinderFactory pathFinderFactory = new PathFinderFactory();

    @Test
    public void shouldFindPathThatAvoidBottomRightCorner() {
        BitMap bitMap = BitMap.createBitMap(
                "  ",
                " #");
        PathFinder pathFinder = pathFinderFactory.createPathFinder(new CollisionBitMap(bitMap));

        Point begin = Point.of(0.9, 1.9);
        Point end = Point.of(1.9, 0.9);

        List<Point> path = pathFinder.findPathToDestination(begin, end);

        assertThat(path).containsExactly(begin, Point.of(1, 1), end);
    }

    @Test
    public void shouldReturnStartPointIfPlayerIsFacingWallAndTryGoToCollidableArea() {
        BitMap bitMap = BitMap.createBitMap(
                "    ",
                " ## ",
                " #  ");
        PathFinder pathFinder = pathFinderFactory.createPathFinder(new CollisionBitMap(bitMap));

        Point begin = Point.of(1, 1.5);
        Point end = Point.of(1.5, 1.5);

        List<Point> path = pathFinder.findPathToDestination(begin, end);

        assertThat(path).containsExactly(begin);
    }

    @Test
    public void shouldFindPathThatAvoidMultipleCorners() {
        BitMap bitMap = BitMap.createBitMap(
                "    ",
                " ## ",
                " #  ");
        PathFinder pathFinder = pathFinderFactory.createPathFinder(new CollisionBitMap(bitMap));

        Point begin = Point.of(0.99, 2.99);
        Point end = Point.of(2, 2);

        List<Point> path = pathFinder.findPathToDestination(begin, end);

        assertThat(path).containsExactly(begin, Point.of(1, 1), Point.of(3, 1), Point.of(3, 2), end);
    }

    @Test
    public void shouldFindPathIfBeginAndEndIsInLineOfSight() {
        BitMap bitMap = BitMap.createBitMap(
                "  ",
                " #");
        PathFinder pathFinder = pathFinderFactory.createPathFinder(new CollisionBitMap(bitMap));

        Point begin = Point.of(0.5, 1.5);
        Point end = Point.of(1.5, 0.5);

        List<Point> path = pathFinder.findPathToDestination(begin, end);

        assertThat(path).containsExactly(begin, end);
    }

    @Test
    public void shouldFindPathAndIgnoreFreePlacesInClosedSpace() {
        BitMap bitMap = BitMap.createBitMap(
                "       ",
                " ##### ",
                " #   # ",
                " # # # ",
                " #   # ",
                " ##### ",
                "       ");
        PathFinder pathFinder = pathFinderFactory.createPathFinder(new CollisionBitMap(bitMap));

        Point begin = Point.of(0.5, 0.5);
        Point end = Point.of(6.5, 6.0);

        List<Point> path = pathFinder.findPathToDestination(begin, end);

        assertThat(path).containsExactly(begin, Point.of(6, 1), end);
    }

    @Test
    public void shouldFindPathInsideInClosedSpace() {
        BitMap bitMap = BitMap.createBitMap(
                "       ",
                " ##### ",
                " #   # ",
                " # # # ",
                " #   # ",
                " ##### ",
                "       ");
        PathFinder pathFinder = pathFinderFactory.createPathFinder(new CollisionBitMap(bitMap));

        Point begin = Point.of(2.9, 2.9);
        Point end = Point.of(4.1, 3.9);

        List<Point> path = pathFinder.findPathToDestination(begin, end);

        assertThat(path).containsExactly(begin, Point.of(4, 3), end);
    }

    @Test
    public void shouldGoToNearestDestinationPossiblePositionThatWasOnTheMoveLine() {
        BitMap bitMap = BitMap.createBitMap(
                "      ",
                " # # #",
                "      ");
        PathFinder pathFinder = pathFinderFactory.createPathFinder(new CollisionBitMap(bitMap));

        Point begin = Point.of(0.5, 1.1);
        Point end = Point.of(5.5, 1.1);

        List<Point> path = pathFinder.findPathToDestination(begin, end);

        assertThat(path).containsExactly(begin, Point.of(1, 1), Point.of(4, 1), Point.of(5, 1.1));
    }

    @Test
    public void shouldFindPathIfPositionIsOnBorderButTilePositionIsOutOfMovableArea() {
        BitMap bitMap = BitMap.createBitMap(
                "   ",
                " # ",
                "   ");
        PathFinder pathFinder = pathFinderFactory.createPathFinder(new CollisionBitMap(bitMap));

        Point begin = Point.of(1, 1);
        Point end = Point.of(2.5, 2);

        List<Point> path = pathFinder.findPathToDestination(begin, end);

        assertThat(path).containsExactly(begin, Point.of(2, 1), end);
    }
}