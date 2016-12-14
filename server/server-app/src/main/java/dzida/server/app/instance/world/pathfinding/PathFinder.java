package dzida.server.app.instance.world.pathfinding;

import com.google.common.collect.ImmutableList;
import dzida.server.app.basic.unit.Geometry2D;
import dzida.server.app.basic.unit.Graph;
import dzida.server.app.basic.unit.Point;
import dzida.server.app.basic.unit.PointList;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Pathfinder that use graph with points in line of sight and A* algorithm to find the shortest path.
 * For more information see: http://www.david-gouveia.com/portfolio/pathfinding-on-a-2d-polygonal-map.
 */
public class PathFinder {
    private final CollisionMap collisionMap;

    public PathFinder(CollisionMap collisionMap) {
        this.collisionMap = collisionMap;
    }

    public List<Point> findPathToDestination(Point begin, Point end) {
        Optional<CollisionMap.MovableArea> childPolygonOpt = collisionMap.getMovableAreaForPosition(begin);

        if (!childPolygonOpt.isPresent()) {
            // If player somehow mange to be in collidable block. Ignore collision for him.
            // That open space for many overuses/abuses.
            return ImmutableList.of(begin, end);
        }
        CollisionMap.MovableArea movableArea = childPolygonOpt.get();

        Point reachableDestination = getEndPoint(begin.getX(), begin.getY(), end.getX(), end.getY(), movableArea);
        if (reachableDestination.equals(begin)) {
            return Collections.singletonList(begin);
        }

        if (isInLineOfSight(begin, reachableDestination, movableArea)) {
            return ImmutableList.of(begin, reachableDestination);
        }

        Graph<Point> enrichedLineOfSightGraph = addMoveToGraph(begin, reachableDestination, movableArea);
        return AStar.findShortestPath(begin, reachableDestination, enrichedLineOfSightGraph);
    }

    private Graph<Point> addMoveToGraph(Point begin, Point destination, CollisionMap.MovableArea movableArea) {
        Graph<Point> lineOfSightGraph = movableArea.getLineOfSightGraph();
        List<Point> convexPoints = lineOfSightGraph.getAllNodes();

        // Only points within some range from begging and end could be check, that would significantly improve the performance of path finding.
        List<Point> pointsFromStart = findPointsInLineOfSight(begin, convexPoints, movableArea);
        List<Point> pointsToEnd = findPointsInLineOfSight(destination, convexPoints, movableArea);
        Graph.Builder<Point> builder = Graph.builder(lineOfSightGraph);
        builder.put(begin, pointsFromStart);
        pointsToEnd.forEach(point -> builder.put(point, destination));

        return builder.build();
    }

    private List<Point> findPointsInLineOfSight(Point point, List<Point> convexPoints, CollisionMap.MovableArea polygon) {
        return convexPoints.stream().filter(p2 -> isInLineOfSight(point, p2, polygon)).collect(Collectors.toList());
    }

    private Point getEndPoint(double sx, double sy, double dx, double dy, CollisionMap.MovableArea polygon) {
        if (polygon.getPolygon().isInside(dx, dy)) {
            return new Point(dx, dy);
        } else {
            return findClosestReachableEndPoint(sx, sy, dx, dy, polygon);
        }
    }

    private Point findClosestReachableEndPoint(double sx, double sy, double dx, double dy, CollisionMap.MovableArea polygon) {
        PointList.Builder builder = PointList.builder();
        builder.add(sx, sy);
        builder.add(polygon.getPolygon().getIntersections(sx, sy, dx, dy));
        polygon.getCollisionBlocks().forEach(collisionBlock -> builder.add(collisionBlock.getPolygon().getIntersections(sx, sy, dx, dy)));
        PointList pointList = builder.build();

        double closesDistance = Double.MAX_VALUE;
        int nearestCollationPointToEnd = 0;
        for (int i = 0; i< pointList.size(); i++) {
            double distanceSqr = Geometry2D.distanceSqr(pointList.x(i), pointList.y(i), dx, dy);
            if (distanceSqr < closesDistance) {
                closesDistance = distanceSqr;
                nearestCollationPointToEnd = i;
            }
        }

        // assuming that end is outside of movable area so move line will crossed the boarded.
        return new Point(pointList.x(nearestCollationPointToEnd), pointList.y(nearestCollationPointToEnd));
    }

    private boolean isInLineOfSight(Point begin, Point end, CollisionMap.MovableArea polygon) {
        return polygon.getPolygon().isLineInside(begin.getX(), begin.getY(), end.getX(), end.getY()) &&
                polygon.getCollisionBlocks().stream().allMatch(child -> child.getPolygon().isLineOutside(begin.getX(), begin.getY(), end.getX(), end.getY()));
    }

}
