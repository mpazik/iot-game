package dzida.server.core.world;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import dzida.server.core.basic.unit.Point;
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

        Multimap<Point, Point> graph = MultimapBuilder.hashKeys().arrayListValues().build();
        graph.put(start, A);
        graph.put(start, B);
        graph.put(A, end);
        graph.put(B, C);
        graph.put(C, end);

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

        Multimap<Point, Point> graph = MultimapBuilder.hashKeys().arrayListValues().build();
        graph.put(start, A);
        graph.put(start, B);
        graph.put(start, D);
        graph.put(A, C);
        graph.put(B, C);
        graph.put(C, end);
        graph.put(D, end);

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

        Multimap<Point, Point> graph = MultimapBuilder.hashKeys().arrayListValues().build();
        graph.put(start, A);
        graph.put(A, B);
        graph.put(B, nearEnd1);
        graph.put(start, nearEnd2);

        List<Point> shortestPath = AStar.findShortestPath(start, end, graph);
        assertThat(shortestPath).containsExactly(start, A, B, nearEnd1);
    }
}