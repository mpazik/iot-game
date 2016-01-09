package dzida.server.core.basic.geometry;

import dzida.server.core.basic.unit.BitMap;
import dzida.server.core.basic.unit.IntPoint;
import dzida.server.core.basic.unit.Point;
import dzida.server.core.basic.unit.Polygon;

import java.util.*;

import static dzida.server.core.basic.geometry.BitMapTracker.Direction.*;

public class BitMapTracker {
    enum Direction {
        TOP,
        RIGHT,
        BOTTOM,
        LEFT
    }

    public Set<Polygon> track(BitMap bitMap) {
        Set<Polygon> polygons = new HashSet<>(0);
        Optional<IntPoint> start = findSetPoint(bitMap);
        start.ifPresent(startPoint -> polygons.add(trackPath(bitMap, startPoint.getX(), startPoint.getY())));

        return polygons;
    }

    private Optional<IntPoint> findSetPoint(BitMap bitMap) {
        for (int y = 0; y < bitMap.getHeight(); y++) {
            for (int x = 0; x < bitMap.getWidth(); x++) {
                if (bitMap.isSet(x, y)) {
                    return Optional.of(new IntPoint(x, y));
                }
            }
        }
        return Optional.empty();
    }

    private Polygon trackPath(BitMap bitMap, int startX, int startY) {
        List<Point> points = new ArrayList<>(4);
        int x = startX;
        int y = startY;
        points.add(new Point(x, y));

        // we are assuming that start position is top left corner;
        x += 1;
        Direction direction = RIGHT;

        while (!(x == startX && y == startY)) {
            switch (direction) {
                case TOP:
                    if (!bitMap.isSet(x, y - 1)) {
                        direction = RIGHT;
                        points.add(new Point(x, y));
                        x += 1;
                        break;
                    }
                    if (bitMap.isSet(x - 1, y - 1)) {
                        direction = LEFT;
                        points.add(new Point(x, y));
                        x -= 1;
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
                    if (!bitMap.isSet(x - 1, y - 1)) {
                        direction = TOP;
                        points.add(new Point(x, y));
                        y -= 1;
                        break;
                    }
                    if (bitMap.isSet(x - 1, y)) {
                        direction = BOTTOM;
                        points.add(new Point(x, y));
                        y += 1;
                        break;
                    }

                    x -= 1;
                    break;
            }
        }
        return new Polygon(points);
    }

    private OptionalInt findStart(BitMap bitMap, int y, int x1, int x2) {
        for (int i = x1 + 1; i <= x2; i++) {
            if (!bitMap.isSet(i, y)) {
                continue;
            }
            return OptionalInt.of(i);
        }
        return OptionalInt.empty();
    }

    final static private class HorizontalLine {
        final int start, end;

        public HorizontalLine(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
