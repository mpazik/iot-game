package dzida.server.core.world.pathfinding;

import dzida.server.core.basic.unit.Graph;
import dzida.server.core.basic.unit.Point;

import java.util.List;
import java.util.Optional;

public class CollisionMap {
    private final List<MovableArea> movableAreas;

    CollisionMap(List<MovableArea> movableAreas) {
        this.movableAreas = movableAreas;
    }

    public Optional<MovableArea> getMovableAreaForPosition(Point position) {
        return getMovableAreaForPosition(position, movableAreas);
    }

    private Optional<MovableArea> getMovableAreaForPosition(Point position, List<MovableArea> movableAreas) {
        Optional<MovableArea> movableAreaForPosition = movableAreas.stream()
                .filter(movableArea -> {
                    Polygon polygon = movableArea.getPolygon();
                    return polygon.isInside(position.getX(), position.getY()) || polygon.isOnBorder(position);
                })
                .findAny();

        if (!movableAreaForPosition.isPresent()) {
            return movableAreaForPosition;
        }
        Optional<CollisionBlock> blackPolygon = movableAreaForPosition.get().getCollisionBlocks().stream()
                .filter(block -> {
                    Polygon polygon = block.getPolygon();
                    return polygon.isInside(position.getX(), position.getY()) && !polygon.isOnBorder(position);
                })
                .findAny();

        return blackPolygon.map(p -> getMovableAreaForPosition(position, p.getMovableAreas())).orElse(movableAreaForPosition);
    }

    public List<MovableArea> getMovableAreas() {
        return movableAreas;
    }

    static class MovableArea {
        private final Polygon polygon;
        private final List<CollisionBlock> childPolygons;
        private final Graph<Point> lineOfSightGraph;

        MovableArea(Polygon polygon, List<CollisionBlock> childPolygons, Graph<Point> lineOfSightGraph) {
            this.polygon = polygon;
            this.childPolygons = childPolygons;
            this.lineOfSightGraph = lineOfSightGraph;
        }

        public Polygon getPolygon() {
            return polygon;
        }

        Graph<Point> getLineOfSightGraph() {
            return lineOfSightGraph;
        }

        List<CollisionBlock> getCollisionBlocks() {
            return childPolygons;
        }
    }

    static class CollisionBlock {
        private final Polygon polygon;
        private final List<MovableArea> childPolygons;

        CollisionBlock(Polygon polygon, List<MovableArea> childPolygons) {
            this.polygon = polygon;
            this.childPolygons = childPolygons;
        }

        public Polygon getPolygon() {
            return polygon;
        }

        List<MovableArea> getMovableAreas() {
            return childPolygons;
        }
    }
}
