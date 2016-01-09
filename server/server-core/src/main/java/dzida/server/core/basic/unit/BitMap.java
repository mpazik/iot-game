package dzida.server.core.basic.unit;

import java.util.BitSet;

public class BitMap {
    private final BitSet bitSet;
    private final int width;
    private final int height;

    public BitMap(BitSet bitSet, int width, int height) {
        this.bitSet = bitSet;
        this.width = width;
        this.height = height;
    }

    public boolean isSet(int x, int y) {
        return isOnBitMap(x, y) && bitSet.get(bitSetPos(x, y));

    }

    public boolean isOnBitMap(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
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

    final static public class Builder {
        private final BitSet bitSet;
        private final int width;
        private final int height;

        public Builder(int width, int height) {
            bitSet = new BitSet(width * height);
            this.width = width;
            this.height = height;
        }

        public Builder set(int x, int y) {
            return set(x, y, true);
        }

        public Builder set(int x, int y, boolean value) {
            bitSet.set(bitSetPos(x, y), value);
            return this;
        }

        public BitMap build() {
            return new BitMap(bitSet, width, height);
        }

        private int bitSetPos(int x, int y) {
            return y * width + x;
        }
    }
}
