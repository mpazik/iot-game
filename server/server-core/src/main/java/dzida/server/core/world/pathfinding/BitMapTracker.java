package dzida.server.core.world.pathfinding;

import com.google.common.collect.ImmutableList;
import dzida.server.core.basic.unit.BitMap;
import dzida.server.core.basic.unit.Point;
import dzida.server.core.basic.unit.PointList;
import dzida.server.core.basic.unit.TreeNode;

import java.util.ArrayList;
import java.util.List;

import static dzida.server.core.world.pathfinding.BitMapTracker.Direction.*;

public class BitMapTracker {
    enum Direction {
        TOP,
        RIGHT,
        BOTTOM,
        LEFT
    }

    public List<TreeNode<Polygon>> track(BitMap bitMap) {
        List<TreeNode<Polygon>> polygons = new ArrayList<>();

        bitMap.forEach((x, y) -> {
            // added epsilon since is isInside doesn't count polygon borders
            if (!polygons.stream().anyMatch(p -> p.getData().isInsideTile(x, y))) {
                TreeNode<Polygon> polygon = trackPath(bitMap, x, y);
                polygons.add(polygon);
            }
        });

        return ImmutableList.copyOf(polygons);
    }

    private TreeNode<Polygon> trackPath(BitMap bitMap, int startX, int startY) {
        PointList.Builder points = PointList.builder();

        int x = startX;
        int y = startY;
        Point point = new Point(x, y);
        points.add(point);
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
                        points.add(new Point(x, y));
                        x -= 1;
                        break;
                    }
                    if (!bitMap.isSet(x, y - 1)) {
                        direction = RIGHT;
                        points.add(new Point(x, y));
                        x += 1;
                        break;
                    }
                    y -= 1;
                    break;
                case BOTTOM:
                    maxY = Math.max(maxY, y);
                    if (bitMap.isSet(x, y)) {
                        direction = RIGHT;
                        points.add(new Point(x, y));
                        x += 1;
                        break;
                    }
                    if (!bitMap.isSet(x - 1, y)) {
                        direction = LEFT;
                        points.add(new Point(x, y));
                        x -= 1;
                        break;
                    }
                    y += 1;
                    break;
                case RIGHT:
                    maxX = Math.max(maxX, x);
                    if (bitMap.isSet(x, y - 1)) {
                        direction = TOP;
                        points.add(new Point(x, y));
                        y -= 1;
                        break;
                    }
                    if (!bitMap.isSet(x, y)) {
                        direction = BOTTOM;
                        points.add(new Point(x, y));
                        y += 1;
                        break;
                    }
                    x += 1;
                    break;
                case LEFT:
                    minX = Math.min(minX, x);
                    if (bitMap.isSet(x - 1, y)) {
                        direction = BOTTOM;
                        points.add(new Point(x, y));
                        y += 1;
                        break;
                    }
                    if (!bitMap.isSet(x - 1, y - 1)) {
                        direction = TOP;
                        points.add(new Point(x, y));
                        y -= 1;
                        break;
                    }
                    x -= 1;
                    break;
            }
        }

        Polygon polygon = new Polygon(points.build());

        // +1 and -2 because we do not need to track borders.
        List<TreeNode<Polygon>> children = track(new PolygonBitMap(minX, minY, maxX - startX, maxY - startY, bitMap, polygon));
        return new TreeNode<>(polygon, children);
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
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }
    }
}
