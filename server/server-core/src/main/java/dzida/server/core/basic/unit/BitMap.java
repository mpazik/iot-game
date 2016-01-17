package dzida.server.core.basic.unit;

import java.util.BitSet;

public interface BitMap {

    boolean isSetUnsafe(int x, int y);

    int getWidth();

    int getHeight();

    default boolean isSet(int x, int y) {
        return isOnBitMap(x, y) && isSetUnsafe(x, y);
    }

    default boolean isOnBitMap(int x, int y) {
        return x >= 0 && x < getWidth() && y >= 0 && y < getHeight();
    }

    default void forEach(Points.IntPointOperator function) {
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
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

        public InverseBitMap(BitMap bitMap) {
            this.bitMap = bitMap;
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
    }

    class ImmutableBitMap implements BitMap {
        private final BitSet bitSet;
        private final int width;
        private final int height;

        private ImmutableBitMap(BitSet bitSet, int width, int height) {
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

            public Builder(int width, int height) {
                bitSet = new BitSet(width * height);
                this.width = width;
                this.height = height;
            }

            public Builder set(int x, int y, boolean value) {
                if (isOnBitMap(x, y)) {
                    bitSet.set(bitSetPos(x, y), value);
                }
                return this;
            }

            public BitMap build() {
                return new ImmutableBitMap(bitSet, width, height);
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