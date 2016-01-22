package dzida.server.core.world.pathfinding;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import dzida.server.core.basic.unit.BitMap;
import dzida.server.core.basic.unit.Line;
import dzida.server.core.basic.unit.Point;
import dzida.server.core.basic.unit.TreeNode;
import dzida.server.core.world.pathfinding.PathFinder.CollisionBlock;
import dzida.server.core.world.pathfinding.PathFinder.MovableArea;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PathFinderFactory {
    public PathFinder createPathFinder(CollisionBitMap collisionBitMap) {
        BitMapTracker bitMapTracker = new BitMapTracker();
        BitMap movableBitMap = BitMap.InverseBitMap.of(collisionBitMap.toBitMap());
        List<TreeNode<Polygon>> movablePolygons = bitMapTracker.track(movableBitMap);

        List<MovableArea> movableAreas = movablePolygons.stream().map(this::createMovableArea).collect(Collectors.toList());

        PathFinder.CollisionMap collisionMap = new PathFinder.CollisionMap(movableAreas);
        return new PathFinder(collisionMap);
    }

    public MovableArea createMovableArea(TreeNode<Polygon> polygonNode) {
        Polygon polygon = polygonNode.getData();
        List<CollisionBlock> collisionBlocks = polygonNode.getChildren().stream().map(this::createCollisionBlock).collect(Collectors.toList());
        return new MovableArea(polygon, collisionBlocks, createGraphOfVisibility(polygon, collisionBlocks));
    }

    public CollisionBlock createCollisionBlock(TreeNode<Polygon> polygonNode) {
        List<MovableArea> movableAreas = polygonNode.getChildren().stream().map(this::createMovableArea).collect(Collectors.toList());
        return new CollisionBlock(polygonNode.getData(), movableAreas);
    }

    public Multimap<Point, Point> createGraphOfVisibility(Polygon polygon, List<CollisionBlock> collisionBlocks) {
        Stream<Point> convexPointsOfCollisionBlocks = collisionBlocks.stream()
                .flatMap(collisionBlock -> collisionBlock.getPolygon().getConvexPoints().stream());
        Stream<Point> convexPointsOfMainBlock = polygon.getConcavePoints().stream();
        List<Polygon> collisionPolygons = collisionBlocks.stream().map(CollisionBlock::getPolygon).collect(Collectors.toList());
        List<Point> convexPoints = Stream.concat(convexPointsOfMainBlock, convexPointsOfCollisionBlocks).collect(Collectors.toList());
        Multimap<Point, Point> graph = MultimapBuilder.hashKeys().arrayListValues().build();
        convexPoints.forEach(point -> graph.putAll(point, findPointsInLineOfSight(point, convexPoints, polygon, collisionPolygons)));
        return graph;
    }

    private Iterable<Point> findPointsInLineOfSight(Point point, List<Point> convexPoints, Polygon polygon, List<Polygon> collisionPolygons) {
        return convexPoints.stream().filter(p2 -> {
            Line line = new Line(point, p2);
            return !polygon.intersectInside(line) && collisionPolygons.stream().allMatch(p -> !p.intersect(line));
        }).collect(Collectors.toList());
    }
}
