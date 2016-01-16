package dzida.server.core.world.pathfinding;

import com.google.common.collect.ImmutableList;
import dzida.server.core.basic.unit.BitMap;
import dzida.server.core.basic.unit.Point;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dzida.server.core.world.pathfinding.BitMapTracker.Direction.*;

public class BitMapTracker {
    enum Direction {
        TOP,
        RIGHT,
        BOTTOM,
        LEFT
    }

    public Set<Polygon> track(BitMap bitMap) {
        Set<Polygon> polygons = new HashSet<>(0);

        BitMap.Builder alreadyVisited = new BitMap.Builder(bitMap.getWidth(), bitMap.getHeight());
        bitMap.forEach((x, y) -> {
            if (!alreadyVisited.isSet(x, y)) {
                Polygon polygon = trackPath(bitMap, x, y);
                polygons.add(polygon);
                alreadyVisited.set(polygon.getPolygonMap(), polygon.getX(), polygon.getY());
            }
        });

        return polygons;
    }

    private Polygon trackPath(BitMap bitMap, int startX, int startY) {
        List<Point> points = new ArrayList<>(20);
        List<Point> concave = new ArrayList<>(10);
        List<Point> convex = new ArrayList<>(10);
        BitMap.Builder borderMapBuilder = new BitMap.Builder(bitMap.getWidth() - startX, bitMap.getHeight() - startY);

        int x = startX;
        int y = startY;
        Point point = new Point(x, y);
        points.add(point);
        convex.add(point);
        borderMapBuilder.set(x - startX, y - startY);
        // we are assuming that start position is top left corner;
        x += 1;
        Direction direction = RIGHT;

        while (!(x == startX && y == startY)) {
            switch (direction) {
                case TOP:
                    if (!bitMap.isSet(x, y - 1)) {
                        direction = RIGHT;
                        Point pt1 = new Point(x, y);
                        points.add(pt1);
                        concave.add(pt1);
                        x += 1;
                        break;
                    }
                    if (bitMap.isSet(x - 1, y - 1)) {
                        direction = LEFT;
                        Point pt2 = new Point(x, y);
                        points.add(pt2);
                        convex.add(pt2);
                        borderMapBuilder.set(x - 1 - startX, y - 1 - startY);
                        x -= 1;
                        break;
                    }
                    borderMapBuilder.set(x - startX, y - 1 - startY);
                    y -= 1;
                    break;
                case BOTTOM:
                    if (bitMap.isSet(x, y)) {
                        direction = RIGHT;
                        points.add(new Point(x, y));
                        borderMapBuilder.set(x - startX, y - startY);
                        x += 1;
                        break;
                    }
                    if (!bitMap.isSet(x - 1, y)) {
                        direction = LEFT;
                        point = new Point(x, y);
                        points.add(point);
                        convex.add(point);
                        x -= 1;
                        break;
                    }
                    borderMapBuilder.set(x - 1 - startX, y - startY);
                    y += 1;
                    break;
                case RIGHT:
                    if (bitMap.isSet(x, y - 1)) {
                        direction = TOP;
                        points.add(new Point(x, y));
                        borderMapBuilder.set(x - startX, y - 1 - startY);
                        y -= 1;
                        break;
                    }
                    if (!bitMap.isSet(x, y)) {
                        direction = BOTTOM;
                        point = new Point(x, y);
                        points.add(point);
                        convex.add(point);
                        y += 1;
                        break;
                    }
                    borderMapBuilder.set(x - startX, y - startY);
                    x += 1;
                    break;
                case LEFT:
                    if (!bitMap.isSet(x - 1, y - 1)) {
                        direction = TOP;
                        point = new Point(x, y);
                        points.add(point);
                        convex.add(point);
                        y -= 1;
                        break;
                    }
                    if (bitMap.isSet(x - 1, y)) {
                        direction = BOTTOM;
                        point = new Point(x, y);
                        points.add(point);

                        borderMapBuilder.set(x - 1 - startX, y - startY);
                        y += 1;
                        break;
                    }
                    borderMapBuilder.set(x - 1 - startX, y - 1 - startY);
                    x -= 1;
                    break;
            }
        }


        BitMap borderMap = borderMapBuilder.crop().build();
        BitMap.Builder polygonMapBuilder = BitMap.builder(borderMap.getWidth(), borderMap.getHeight());
        for (int bx=0; bx<borderMap.getWidth(); bx++) {
            for (int by=0; by<borderMap.getHeight(); by++) {
                if (borderMap.isSet(bx, by) || (bitMap.isSet(bx, by) && isNextToSet(polygonMapBuilder, bx, by) )) {
                    polygonMapBuilder.set(bx, by);
                }
            }
        }

        return new Polygon(ImmutableList.copyOf(points),
                polygonMapBuilder.build(),
                startX, startY,
                ImmutableList.copyOf(concave),
                ImmutableList.copyOf(convex)
        );
    }

    boolean isNextToSet(BitMap.Builder bitMap, int x, int y) {
        return bitMap.isSet(x - 1, y) || bitMap.isSet(x + 1, y) ||
                bitMap.isSet(x, y - 1) || bitMap.isSet(x, y + 1);
    }
}
