package dzida.server.core.basic.unit;

import com.google.common.base.Objects;

import java.util.BitSet;

public class BitMap {
    private final BitSet bitSet;
    private final int width;
    private final int height;

    private BitMap(BitSet bitSet, int width, int height) {
        this.bitSet = BitSet.valueOf(bitSet.toLongArray());
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

    public void forEach(Points.IntPointOperator function) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (isSet(x, y)) {
                    function.apply(x, y);
                }
            }
        }
    }

    private int bitSetPos(int x, int y) {
        return y * width + x;
    }

    public static Builder builder(int width, int height) {
        return new Builder(width, height);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BitMap bitMap = (BitMap) o;
        return width == bitMap.width &&
                height == bitMap.height &&
                Objects.equal(bitSet, bitMap.bitSet);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(bitSet, width, height);
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

        public boolean isSet(int x, int y) {
            return isOnBitMap(x, y) && bitSet.get(bitSetPos(x, y));
        }

        public boolean isOnBitMap(int x, int y) {
            return x >= 0 && x < width && y >= 0 && y < height;
        }

        public Builder set(int x, int y) {
            return set(x, y, true);
        }

        public Builder set(int x, int y, boolean value) {
            if (isOnBitMap(x, y)) {
                bitSet.set(bitSetPos(x, y), value);
            }
            return this;
        }

        public Builder crop() {
            int maxX = 0;
            int maxY = 0;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (isSet(x, y)) {
                        maxX = Math.max(maxX, x + 1);
                        maxY = Math.max(maxY, y + 1);
                    }
                }
            }
            return new Builder(maxX, maxY).set(build(), 0, 0);
        }

        public BitMap build() {
            return new BitMap(bitSet, width, height);
        }

        private int bitSetPos(int x, int y) {
            return y * width + x;
        }

        public Builder set(BitMap bitMap, int offsetX, int offsetY) {
            bitMap.forEach((x, y) -> set(x + offsetX, y + offsetY, bitMap.isSet(x, y)));
            return this;
        }
    }
}
