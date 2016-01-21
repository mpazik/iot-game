package dzida.server.core.basic.unit;

import java.util.BitSet;

public interface BitMap {

    boolean isSetUnsafe(int x, int y);

    int getWidth();

    int getHeight();

    int getX();

    int getY();

    default boolean isSet(int x, int y) {
        return isOnBitMap(x, y) && isSetUnsafe(x, y);
    }

    default boolean isOnBitMap(int x, int y) {
        return x >= getX() && x < getX() + getWidth() && y >= getY() && y < getY() + getHeight();
    }

    default void forEach(Points.IntPointOperator function) {
        for (int y = getY(); y < getY() + getHeight(); y++) {
            for (int x = getX(); x < getX() + getWidth(); x++) {
                if (isSet(x, y)) {
                    function.apply(x, y);
                }
            }
        }
    }

    @SuppressWarnings("unused")
    static String visualise(BitMap bitMap) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int y = 0; y < bitMap.getHeight(); y++) {
            for (int x = 0; x < bitMap.getWidth(); x++) {
                stringBuilder.append(bitMap.isSet(x, y) ? '#' : ' ');
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
            return !bitMap.isSet(x, y);
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
        public int getX() {
            return bitMap.getX();
        }

        @Override
        public int getY() {
            return bitMap.getY();
        }
    }

    class ImmutableBitMap implements BitMap {
        private final BitSet bitSet;
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        private ImmutableBitMap(BitSet bitSet, int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.bitSet = BitSet.valueOf(bitSet.toLongArray());
            this.width = width;
            this.height = height;
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
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        private int bitSetPos(int x, int y) {
            return y * width + x;
        }

        public static Builder builder(int width, int height) {
            return new Builder(width, height);
        }

        @Override
        public String toString() {
            return "BitMap{" +
                    "bitSet=" + bitSet +
                    ", width=" + width +
                    ", height=" + height +
                    '}';
        }

        final static public class Builder {
            private final BitSet bitSet;
            private final int width;
            private final int height;
            private int x = 0;
            private int y = 0;

            public Builder(int width, int height) {
                bitSet = new BitSet(width * height);
                this.width = width;
                this.height = height;
            }

            public Builder setCords(int x, int y) {
                this.x = x;
                this.y = y;
                return this;
            }

            public Builder set(int x, int y, boolean value) {
                if (isOnBitMap(x, y)) {
                    bitSet.set(bitSetPos(x, y), value);
                }
                return this;
            }

            public BitMap build() {
                return new ImmutableBitMap(bitSet, x, y, width, height);
            }

            private boolean isOnBitMap(int x, int y) {
                return x >= 0 && x < width && y >= 0 && y < height;
            }

            private int bitSetPos(int x, int y) {
                return y * width + x;
            }
        }
    }
}