package dzida.server.core.world.pathfinding;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import dzida.server.core.basic.unit.BitMap;
import dzida.server.core.basic.unit.Line;
import dzida.server.core.basic.unit.Point;
import dzida.server.core.world.pathfinding.PathFinder.MovableArea;

import java.util.Set;
import java.util.stream.Collectors;

public class PathFinderFactory {
    public PathFinder createPathFinder(CollisionBitMap collisionBitMap) {
        BitMapTracker bitMapTracker = new BitMapTracker();
        BitMap movableBitMap = new BitMap.InverseBitMap(collisionBitMap.toBitMap());
        Set<Polygon> movablePolygons = bitMapTracker.track(movableBitMap);

        Set<MovableArea> movableAreas = movablePolygons.stream().map(this::createMovableArea).collect(Collectors.toSet());

        PathFinder.CollisionMap collisionMap = new PathFinder.CollisionMap(movableAreas, collisionBitMap);
        return new PathFinder(collisionMap);
    }

    public MovableArea createMovableArea(Polygon polygon) {
        return new MovableArea(polygon.getPoints(), createGraphOfVisibility(polygon));
    }

    public Multimap<Point, Point> createGraphOfVisibility(Polygon polygon) {
        Set<Point> convexPoints = polygon.getConcavePoints();
        Multimap<Point, Point> graph = MultimapBuilder.hashKeys().arrayListValues().build();
        convexPoints.forEach(point -> graph.putAll(point, findPointsInLineOfSight(point, convexPoints, polygon)));
        return graph;
    }

    private Iterable<Point> findPointsInLineOfSight(Point point, Set<Point> convexPoints, Polygon polygon) {
        return convexPoints.stream().filter(p2 -> {
            Line line = new Line(point, p2);
            return !polygon.intersectInside(line);
        }).collect(Collectors.toList());
    }
}
