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

        bitMap.forEach((x, y) -> {
            if (!polygons.stream().anyMatch(p -> p.isInside(x, y))) {
                Polygon polygon = trackPath(bitMap, x, y);
                polygons.add(polygon);
            }
        });

        return polygons;
    }

    private Polygon trackPath(BitMap bitMap, int startX, int startY) {
        List<Point> points = new ArrayList<>(20);

        int x = startX;
        int y = startY;
        Point point = new Point(x, y);
        points.add(point);
        // we are assuming that start position is top left corner;
        x += 1;
        Direction direction = RIGHT;

        while (!(x == startX && y == startY)) {
            switch (direction) {
                case TOP:
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

        return new Polygon(ImmutableList.copyOf(points));
    }
}
