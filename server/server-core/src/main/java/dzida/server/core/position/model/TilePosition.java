package dzida.server.core.position.model;

public final class TilePosition {
    private final int x;
    private final int y;

    public TilePosition(int x, int y) {
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
