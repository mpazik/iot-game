package dzida.server.app.instance.world;

import dzida.server.app.basic.unit.Graph;
import dzida.server.app.basic.unit.Point;
import dzida.server.app.instance.world.pathfinding.AStar;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AStarTest {

    @Test
    public void shortestPathIsReturnedForNotTrivialCase() throws Exception {
        Point start = new Point(0, 0);
        Point A = new Point(0, 2); // is a better neighbor then B in first iteration but because it lead to the C it's worst overall.
        Point B = new Point(2, 0);
        Point C = new Point(2, 6);
        Point end = new Point(10, 0);

        Graph<Point> graph = new Graph.Builder<Point>()
                .put(start, A)
                .put(start, B)
                .put(A, end)
                .put(B, C)
                .put(C, end)
                .build();

        List<Point> shortestPath = AStar.findShortestPath(start, end, graph);
        assertThat(shortestPath).containsExactly(start, A, end);
    }

    @Test
    public void recalculatesShortestDistanceToStartOfNodes() {
        Point start = new Point(0, 0);
        Point A = new Point(5, 0); // is the closes point to the end but shortest path goes should go through the B.
        Point B = new Point(3, 3);
        Point C = new Point(5, 5); // point to which two points led
        Point D = new Point(5, 6); // the shortest path would go through this point if distance to the D wouldn't be recalculated
        Point end = new Point(9, 0);

        Graph<Point> graph = new Graph.Builder<Point>()
                .put(start, A)
                .put(start, B)
                .put(start, D)
                .put(A, C)
                .put(B, C)
                .put(C, end)
                .put(D, end)
                .build();

        List<Point> shortestPath = AStar.findShortestPath(start, end, graph);
        assertThat(shortestPath).containsExactly(start, B, C, end);
    }

    @Test
    public void returnsTheClosesPointToDestinationIfDestinationIsNotReachable() {
        Point start = new Point(0, 0);
        Point A = new Point(5, 0);
        Point B = new Point(5, 9);
        Point nearEnd1 = new Point(8, 0); // path to this end is much longer than to end 2 but this end is closed to the destination
        Point nearEnd2 = new Point(7, 0);
        Point end = new Point(9, 0);

        Graph<Point> graph = new Graph.Builder<Point>()
                .put(start, A)
                .put(A, B)
                .put(B, nearEnd1)
                .put(start, nearEnd2)
                .build();

        List<Point> shortestPath = AStar.findShortestPath(start, end, graph);
        assertThat(shortestPath).containsExactly(start, A, B, nearEnd1);
    }
}