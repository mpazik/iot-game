package dzida.server.core.world.pathfinding;

import com.google.common.collect.Multimap;
import dzida.server.core.basic.unit.Point;

import java.util.*;

public class AStar {
    private final Point start;
    private final Point end;
    private final Multimap<Point, Point> graph;
    private final Map<Point, Point> cameFrom = new HashMap<>();

    public AStar(Point start, Point end, Multimap<Point, Point> graph) {
        this.start = start;
        this.end = end;
        this.graph = graph;
    }

    public static List<Point> findShortestPath(Point start, Point end, Multimap<Point, Point> graph) {
        return new AStar(start, end, graph).findShortestPath();
    }

    private List<Point> findShortestPath() {
        Set<Point> nodesEvaluated = new HashSet<>();

        Map<Point, Double> distanceFromStart = new HashMap<>(); // Shortest distances to the start point from the given points.
        Map<Point, Double> distanceToEnd = new HashMap<>(); // Shortest heuristic distances to the end from the given points.

        Comparator<Point> heuristicDistanceToEndComparator = (p1, p2) -> Math.toIntExact(Math.round(distanceToEnd.get(p1) - distanceToEnd.get(p2)));
        PriorityQueue<Point> nodesToEvaluate = new PriorityQueue<>(heuristicDistanceToEndComparator);


        distanceFromStart.put(start, 0.0);
        distanceToEnd.put(start, getStraitDistanceToEnd(start));
        nodesToEvaluate.add(start);


        while (!nodesToEvaluate.isEmpty()) {
            Point current = nodesToEvaluate.poll();
            if (current.equals(end)) {
                return reconstructPath(end);
            }
            nodesEvaluated.add(current);

            for (Point neighbor : graph.get(current)) {
                if (nodesEvaluated.contains(neighbor)) continue;

                double distanceBetweenTwoNodes = current.distanceTo(neighbor);
                double lengthOfCurrentPath = distanceFromStart.get(current) + distanceBetweenTwoNodes;

                if (!distanceFromStart.containsKey(neighbor) || lengthOfCurrentPath < distanceFromStart.get(neighbor)) {
                    distanceFromStart.put(neighbor, lengthOfCurrentPath);
                    distanceToEnd.put(neighbor, lengthOfCurrentPath + getStraitDistanceToEnd(neighbor));

                    cameFrom.put(neighbor, current);
                    if (nodesToEvaluate.contains(neighbor)) {
                        // update priority of the neighbor
                        nodesToEvaluate.remove(neighbor);
                        nodesToEvaluate.add(neighbor);
                    } else {
                        nodesToEvaluate.add(neighbor);
                    }
                }
            }
        }

        Comparator<Point> straitDistanceToEndComparator = (p1, p2) -> Math.toIntExact(Math.round(getStraitDistanceToEnd(p1) - getStraitDistanceToEnd(p2)));
        Point closestEnd = nodesEvaluated.stream().sorted(straitDistanceToEndComparator).findFirst().orElse(start);
        return reconstructPath(closestEnd);
    }

    private double getStraitDistanceToEnd(Point neighbor) {
        return neighbor.distanceTo(end);
    }

    private List<Point> reconstructPath(Point destination) {
        assert destination != null;

        final List<Point> pathList = new ArrayList<>();
        pathList.add(destination);
        while (cameFrom.containsKey(destination)) {
            destination = cameFrom.get(destination);
            pathList.add(destination);
        }
        Collections.reverse(pathList);
        return pathList;
    }
}
