package dzida.server.app.instance.world.pathfinding;

import dzida.server.app.basic.unit.BitMap;
import dzida.server.app.basic.unit.Graph;
import dzida.server.app.basic.unit.Point;
import dzida.server.app.basic.unit.TreeNode;
import dzida.server.app.instance.world.pathfinding.CollisionMap.CollisionBlock;
import dzida.server.app.instance.world.pathfinding.CollisionMap.MovableArea;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollisionMapFactory {
    // Determine the line of sight range between points. 0 means the range is infinite.
    private final int lineOfSightRange;

    public CollisionMapFactory(int lineOfSightRange) {
        this.lineOfSightRange = lineOfSightRange;
    }

    @Nonnull
    public CollisionMap createCollisionMap(BitMap collisionBitMap) {
        BitMapTracker bitMapTracker = new BitMapTracker();
        BitMap movableBitMap = BitMap.InverseBitMap.of(collisionBitMap);
        List<TreeNode<Polygon>> movablePolygons = bitMapTracker.track(movableBitMap);

        List<MovableArea> movableAreas = movablePolygons.stream().map(this::createMovableArea).collect(Collectors.toList());

        return new CollisionMap(movableAreas);
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

    public Graph<Point> createGraphOfVisibility(Polygon polygon, List<CollisionBlock> collisionBlocks) {
        Stream<Point> convexPointsOfCollisionBlocks = collisionBlocks.stream()
                .flatMap(collisionBlock -> collisionBlock.getPolygon().getConvexPoints().stream());
        Stream<Point> convexPointsOfMainBlock = polygon.getConcavePoints().stream();
        List<Point> convexPoints = Stream.concat(convexPointsOfMainBlock, convexPointsOfCollisionBlocks).collect(Collectors.toList());

        List<Polygon> collisionPolygons = collisionBlocks.stream().map(CollisionMap.CollisionBlock::getPolygon).collect(Collectors.toList());

        Graph.Builder<Point> graphBuilder = Graph.builder();
        convexPoints.forEach(point -> graphBuilder.put(point, findPointsInLineOfSight(point, convexPoints, polygon, collisionPolygons)));
        return graphBuilder.build();
    }

    private List<Point> findPointsInLineOfSight(Point point, List<Point> convexPoints, Polygon polygon, List<Polygon> collisionPolygons) {

        Predicate<Point> rangeFilter = lineOfSightRange == 0 ? x -> true : p2 -> p2.isInRange(point, lineOfSightRange);
        return convexPoints.stream()
                .filter(rangeFilter)
                .filter(p2 -> polygon.isLineInside(point.getX(), point.getY(), p2.getX(), p2.getY()))
                .filter(p2 -> collisionPolygons.stream().allMatch(p -> p.isLineOutside(point.getX(), point.getY(), p2.getX(), p2.getY())))
                .collect(Collectors.toList());
    }
}
