package dzida.server.core.basic.unit;

import java.util.BitSet;
import java.util.Objects;

public interface BitMap {

    @SuppressWarnings("unused")
    static String visualise(BitMap bitMap) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int y = 0; y < bitMap.getHeight(); y++) {
            for (int x = 0; x < bitMap.getWidth(); x++) {
                stringBuilder.append(bitMap.isSet(x + bitMap.getStartX(), y + bitMap.getStartY()) ? '#' : ' ');
            }
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }

    static BitMap createBitMap(String... rows) {
        int height = rows.length;
        int width = height == 0 ? 0 : rows[0].length();

        ImmutableBitMap.Builder builder = ImmutableBitMap.builder(width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char c = rows[y].charAt(x);
                builder.set(x, y, c == '#');
            }
        }

        return builder.build();
    }

    boolean isSetUnsafe(int x, int y);

    int getWidth();

    int getHeight();

    int getStartX();

    int getStartY();

    default boolean isSet(int x, int y) {
        return isOnBitMap(x, y) && isSetUnsafe(x, y);
    }

    default boolean isOnBitMap(int x, int y) {
        return x >= getStartX() && x < getStartX() + getWidth() && y >= getStartY() && y < getStartY() + getHeight();
    }

    default void forEach(Geometry2D.IntPointOperator function) {
        for (int y = getStartY(); y < getStartY() + getHeight(); y++) {
            for (int x = getStartX(); x < getStartX() + getWidth(); x++) {
                if (isSetUnsafe(x, y)) {
                    function.apply(x, y);
                }
            }
        }
    }

    class InverseBitMap implements BitMap {
        private final BitMap bitMap;

        private InverseBitMap(BitMap bitMap) {
            this.bitMap = bitMap;
        }

        public static BitMap of(BitMap bitMap) {
            if (bitMap instanceof InverseBitMap) {
                return ((InverseBitMap) bitMap).bitMap;
            }
            return new InverseBitMap(bitMap);
        }

        @Override
        public boolean isSetUnsafe(int x, int y) {
            return !bitMap.isSetUnsafe(x, y);
        }

        @Override
        public int getWidth() {
            return bitMap.getWidth();
        }

        @Override
        public int getHeight() {
            return bitMap.getHeight();
        }

        @Override
        public int getStartX() {
            return bitMap.getStartX();
        }

        @Override
        public int getStartY() {
            return bitMap.getStartY();
        }

        public ImmutableBitMap toImmutableBitMap() {
            ImmutableBitMap.Builder builder = ImmutableBitMap.builder(getStartX(), getStartY(), getWidth(), getHeight());
            this.forEach((x, y) -> builder.set(x, y, true));
            return builder.build();
        }
    }

    class ImmutableBitMap implements BitMap {
        private final BitSet bitSet;
        private final int startX;
        private final int startY;
        private final int width;
        private final int height;

        private ImmutableBitMap(BitSet bitSet, int startX, int startY, int width, int height) {
            this.startX = startX;
            this.startY = startY;
            this.bitSet = BitSet.valueOf(bitSet.toLongArray());
            this.width = width;
            this.height = height;
        }

        public static Builder builder(int width, int height) {
            return new Builder(0, 0, width, height);
        }

        public static Builder builder(int startX, int startY, int width, int height) {
            return new Builder(startX, startY, width, height);
        }

        @Override
        public boolean isSetUnsafe(int x, int y) {
            return bitSet.get(bitSetPos(x, y));
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        @Override
        public int getStartX() {
            return startX;
        }

        @Override
        public int getStartY() {
            return startY;
        }

        private int bitSetPos(int x, int y) {
            return (y - startY) * width + (x - startX);
        }

        @Override
        public String toString() {
            return "BitMap{" +
                    "bitSet=" + bitSet +
                    ", width=" + width +
                    ", height=" + height +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ImmutableBitMap that = (ImmutableBitMap) o;
            return startX == that.startX &&
                    startY == that.startY &&
                    width == that.width &&
                    height == that.height &&
                    Objects.equals(bitSet, that.bitSet);
        }

        @Override
        public int hashCode() {
            return Objects.hash(bitSet, startX, startY, width, height);
        }

        final static public class Builder {
            private final BitSet bitSet;
            private final int width;
            private final int height;
            private final int startX;
            private final int startY;

            public Builder(int startX, int y, int width, int height) {
                bitSet = new BitSet(width * height);
                this.width = width;
                this.height = height;
                this.startX = startX;
                this.startY = y;
            }

            public Builder set(int x, int y, boolean value) {
                if (isOnBitMap(x, y)) {
                    bitSet.set(bitSetPos(x, y), value);
                }
                return this;
            }

            public ImmutableBitMap build() {
                return new ImmutableBitMap(bitSet, startX, startY, width, height);
            }

            private boolean isOnBitMap(int x, int y) {
                return x >= startX && x < startX + width && y >= startY && y < startY + height;
            }

            private int bitSetPos(int x, int y) {
                return (y - startY) * width + (x - startX);
            }
        }
    }
}