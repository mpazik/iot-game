package dzida.server.core.world.pathfinding;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import dzida.server.core.basic.unit.Line;
import dzida.server.core.basic.unit.Point;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static dzida.server.core.basic.unit.Points.cordToTail;

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
        int x = cordToTail(begin.getX());
        int y = cordToTail(begin.getY());

        Optional<MovableArea> childPolygonOpt = collisionMap.getMovableAreaForPosition(x, y);

        if (!childPolygonOpt.isPresent()) {
            // Player can not have be inside collidable polygon.
            return Collections.singletonList(begin);
        }
        MovableArea movableArea = childPolygonOpt.get();

        Point reachableEnd = getEndPoint(begin, end, movableArea);
        if (isInLineOfSight(begin, reachableEnd, movableArea)) {
            return ImmutableList.of(begin, reachableEnd);
        }

        Multimap<Point, Point> enrichedLineOfSightGraph = addMoveToGraph(begin, reachableEnd, movableArea);
        return AStar.findShortestPath(begin, reachableEnd, enrichedLineOfSightGraph);
    }

    public Multimap<Point, Point> addMoveToGraph(Point begin, Point end, MovableArea movableArea) {
        Multimap<Point, Point> lineOfSightGraph = movableArea.getLineOfSightGraph();
        Set<Point> convexPoints = lineOfSightGraph.keySet();
        ImmutableMultimap.Builder<Point, Point> enrichedLineOfSightGraphBuilder = ImmutableMultimap.<Point, Point>builder()
                .putAll(lineOfSightGraph)
                .putAll(begin, findPointsInLineOfSight(begin, convexPoints, movableArea));
        findPointsInLineOfSight(end, convexPoints, movableArea).forEach(point -> enrichedLineOfSightGraphBuilder.put(point, end));

        return enrichedLineOfSightGraphBuilder.build();
    }

    private Iterable<Point> findPointsInLineOfSight(Point point, Set<Point> convexPoints, Polygon polygon) {
        return convexPoints.stream().filter(p2 -> {
            Line line = new Line(point, p2);
            return !polygon.intersectInside(line);
        }).collect(Collectors.toList());
    }

    private Point getEndPoint(Point begin, Point end, MovableArea polygon) {
        if (polygon.isInside(end)) {
            return end;
        } else {
            return findClosestReachableEndPoint(begin, end, polygon);
        }
    }

    private Point findClosestReachableEndPoint(Point begin, Point end, MovableArea polygon) {
        // go from end to beginning until find inside but not in child point.
        System.out.println("Not supported yet");
        return begin;
    }

    //    private double cordCount(double conditionValue, double cord) {
//        if (conditionValue < 0) {
//            return Math.floor(cord) - 0.001;
//        } else {
//            if (cord % 1 == 0) return cord + 1;
//            else return Math.ceil(cord);
//        }
//    }
//
//
//    private Point step(Point p, BitMap bitMap, double tailToX, double tailToY, double to_X, double to_Y, double dx, double a) {
//        double x = p.getX();
//        int tailX = cordToTail(x);
//        double y = p.getY();
//        int tailY = cordToTail(y);
//
//            if (bitMap.isSet(tailX, tailY)) {
//                //going to the previus tail
//                if (x % 1 == 0) to_X = x - 0.001;
//                else to_X = x + 0.001;
//                if (y % 1 == 0) to_Y = y - 0.001;
//                else to_Y = y + 0.001;
//                return new Point(to_X, to_Y);
//            }
//
//            if (tailToX == tailX && tailToY == tailY) new Point(to_X, to_Y);
//            //px py - współżędne punku przecięcięcia promienia z krawędzią kafelka
//            double px = cordCount(dx, x);
//            double py = a * (px - x) + y; //obliczenie wysokości przecięcia
//            //jeżeli punkt jest na innym poziomie, czyli dolnej lub górnej krawędzi
//            if (py.floor.toInt != tailY) {
//                py = pyCount(y)
//                px = 1 / a * (py - y) + x;
//            }
//            return step(p, bitMap px, py, tailToX, tailToY, to_X, to_Y, dx, a);
//        }
//    def goFromTo(fromX: Double, fromY: Double, toX: Double, toY: Double): (Double, Double) = {
//
//        if (isCollide(fromX.floor.toInt, fromY.floor.toInt)) {
//            println("player on collision box")
//            return (toX, toY)
//        }
//
//        val dx = toX - fromX
//        val dy = toY - fromY
//        val a = dy / dx
//        val tailToX = toX.floor.toInt
//        val tailToY = toY.floor.toInt
//
//        def pxCount = cordCount(dx) _
//        def pyCount = cordCount(dy) _
//
//        step(fromX, fromY)
//    }

    private boolean isInLineOfSight(Point begin, Point end, MovableArea polygon) {
        Line line = new Line(begin, end);
        return !polygon.intersectInside(line) && !polygon.getCollisionBlocks().stream().anyMatch(child -> child.intersect(line));
    }

    static class CollisionMap {
        private final Set<MovableArea> movableAreas;
        private final CollisionBitMap collisionMap;

        CollisionMap(Set<MovableArea> movableAreas, CollisionBitMap collisionMap) {
            this.movableAreas = movableAreas;
            this.collisionMap = collisionMap;
        }

        public Optional<MovableArea> getMovableAreaForPosition(int x, int y) {
            if (collisionMap.isColliding(x, y)) {
                return Optional.empty();
            }
            return Optional.of(getMovableAreaForPosition(x, y, movableAreas));
        }

        private MovableArea getMovableAreaForPosition(int x, int y, Set<MovableArea> movableAreas) {
            MovableArea movableAreaForPosition = movableAreas.stream()
                    .filter(movableArea -> movableArea.isInside(x, y))
                    .findAny().get();

            Optional<CollisionBlock> blackPolygon = movableAreaForPosition.getCollisionBlocks().stream()
                    .filter(polygon -> polygon.isInside(x, y))
                    .findAny();

            return blackPolygon.map(p -> getMovableAreaForPosition(x, y, p.getMovableAreas())).orElse(movableAreaForPosition);
        }
    }

    static class MovableArea extends Polygon {
        private final List<CollisionBlock> childPolygons;
        private final Multimap<Point, Point> lineOfSightGraph;

        MovableArea(List<Point> points, Multimap<Point, Point> lineOfSightGraph) {
            super(points);
            this.childPolygons = Collections.emptyList();
            this.lineOfSightGraph = lineOfSightGraph;
        }

        Multimap<Point, Point> getLineOfSightGraph() {
            return lineOfSightGraph;
        }

        List<CollisionBlock> getCollisionBlocks() {
            return childPolygons;
        }
    }

    static class CollisionBlock extends Polygon {
        private final Set<MovableArea> childPolygons;

        CollisionBlock(List<Point> points) {
            super(points);
            this.childPolygons = Collections.emptySet();
        }

        Set<MovableArea> getMovableAreas() {
            return childPolygons;
        }
    }

}
