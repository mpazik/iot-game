package dzida.server.core.basic.unit;

public final class TilePoint {
    private final int x;
    private final int y;

    public TilePoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
