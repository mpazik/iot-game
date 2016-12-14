package dzida.server.app.instance.world.pathfinding;

import com.google.common.collect.ImmutableList;
import dzida.server.app.basic.unit.BitMap;
import dzida.server.app.basic.unit.PointList;
import dzida.server.app.basic.unit.TreeNode;

import java.util.ArrayList;
import java.util.List;

import static dzida.server.app.instance.world.pathfinding.BitMapTracker.Direction.BOTTOM;
import static dzida.server.app.instance.world.pathfinding.BitMapTracker.Direction.LEFT;
import static dzida.server.app.instance.world.pathfinding.BitMapTracker.Direction.RIGHT;
import static dzida.server.app.instance.world.pathfinding.BitMapTracker.Direction.TOP;

public class BitMapTracker {
    public List<TreeNode<Polygon>> track(BitMap bitMap) {
        List<TreeNode<Polygon>> polygons = new ArrayList<>();

        bitMap.forEach((x, y) -> {
            if (!isPointOnAnyPolygon(x, y, polygons)) {
                TreeNode<Polygon> polygon = trackPath(bitMap, x, y);
                polygons.add(polygon);
            }
        });

        return ImmutableList.copyOf(polygons);
    }

    private boolean isPointOnAnyPolygon(int x, int y, List<TreeNode<Polygon>> polygons) {
        for (TreeNode<Polygon> polygon : polygons) {
            if (polygon.getData().isInside(x, y)) {
                return true;
            }
        }
        return false;
    }

    private TreeNode<Polygon> trackPath(BitMap bitMap, int startX, int startY) {
        PointList.Builder points = PointList.builder();

        int x = startX;
        int y = startY;
        points.add(x, y);
        // we are assuming that start position is top left corner;
        x += 1;
        int maxX = x, maxY = y, minX = x, minY = y;
        Direction direction = RIGHT;

        while (!(x == startX && y == startY)) {
            switch (direction) {
                case TOP:
                    minY = Math.min(minY, y);
                    if (bitMap.isSet(x - 1, y - 1)) {
                        direction = LEFT;
                        points.add(x, y);
                        x -= 1;
                        break;
                    }
                    if (!bitMap.isSet(x, y - 1)) {
                        direction = RIGHT;
                        points.add(x, y);
                        x += 1;
                        break;
                    }
                    y -= 1;
                    break;
                case BOTTOM:
                    maxY = Math.max(maxY, y);
                    if (bitMap.isSet(x, y)) {
                        direction = RIGHT;
                        points.add(x, y);
                        x += 1;
                        break;
                    }
                    if (!bitMap.isSet(x - 1, y)) {
                        direction = LEFT;
                        points.add(x, y);
                        x -= 1;
                        break;
                    }
                    y += 1;
                    break;
                case RIGHT:
                    maxX = Math.max(maxX, x);
                    if (bitMap.isSet(x, y - 1)) {
                        direction = TOP;
                        points.add(x, y);
                        y -= 1;
                        break;
                    }
                    if (!bitMap.isSet(x, y)) {
                        direction = BOTTOM;
                        points.add(x, y);
                        y += 1;
                        break;
                    }
                    x += 1;
                    break;
                case LEFT:
                    minX = Math.min(minX, x);
                    if (bitMap.isSet(x - 1, y)) {
                        direction = BOTTOM;
                        points.add(x, y);
                        y += 1;
                        break;
                    }
                    if (!bitMap.isSet(x - 1, y - 1)) {
                        direction = TOP;
                        points.add(x, y);
                        y -= 1;
                        break;
                    }
                    x -= 1;
                    break;
            }
        }

        Polygon polygon = new Polygon(points.build());

        // +1 and -2 because we do not need to track borders.
        List<TreeNode<Polygon>> children = track(new PolygonBitMap(minX, minY, maxX - minX, maxY - minY, bitMap, polygon));
        return new TreeNode<>(polygon, children);
    }

    enum Direction {
        TOP,
        RIGHT,
        BOTTOM,
        LEFT
    }

    private static final class PolygonBitMap implements BitMap {
        private final int width, height, x, y;
        private final Polygon polygon;
        private final BitMap worldBitMap;

        private PolygonBitMap(int x, int y, int width, int height, BitMap worldBitMap, Polygon polygon) {
            this.x = x;
            this.y = y;
            this.worldBitMap = InverseBitMap.of(worldBitMap);
            this.polygon = polygon;
            this.width = width;
            this.height = height;
        }

        @Override
        public boolean isSetUnsafe(int x, int y) {
            return worldBitMap.isSet(x, y) && polygon.isInsideTile(x, y);
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public int getStartX() {
            return x;
        }

        @Override
        public int getStartY() {
            return y;
        }
    }
}
