package leveleditor;

import engine.Core;
import engine.Input;
import graphics.Graphics2D;
import graphics.Window2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.lwjgl.input.Keyboard;
import util.Color4;
import static util.Color4.BLACK;
import static util.Color4.WHITE;
import util.Util;
import util.Vec2;

public class Tile {

    public static final int GRID_SIZE = 50;
    public static final double TILE_SIZE = 50;

    public static Tile[][] grid;

    static {
        grid = new Tile[50][50];
        Util.forRange(0, 50, 0, 50, (x, y) -> grid[x][y] = new Tile(x, y));
    }

    public int x, y;
    public Color4 color;
    public Drawable drawable;

    public Tile(int x, int y) {
        this.x = x;
        this.y = y;
        this.color = WHITE;
    }

    public void draw() {
        Graphics2D.fillRect(new Vec2(x, y).multiply(TILE_SIZE), new Vec2(TILE_SIZE), color);
        Graphics2D.drawRect(new Vec2(x, y).multiply(TILE_SIZE), new Vec2(TILE_SIZE), BLACK);
        if (drawable != null) {
            drawable.draw(new Vec2(x + .5, y + .5).multiply(TILE_SIZE));
        }
    }

    public static void drawAll() {
        Util.forRange(0, 50, 0, 50, (x, y) -> grid[x][y].draw());
    }

    public static void init() {
        double speed = 500;
        Input.whileKeyDown(Keyboard.KEY_W).forEach(dt -> Window2D.viewPos = Window2D.viewPos.add(new Vec2(0, speed * dt)));
        Input.whileKeyDown(Keyboard.KEY_A).forEach(dt -> Window2D.viewPos = Window2D.viewPos.add(new Vec2(speed * -dt, 0)));
        Input.whileKeyDown(Keyboard.KEY_S).forEach(dt -> Window2D.viewPos = Window2D.viewPos.add(new Vec2(0, speed * -dt)));
        Input.whileKeyDown(Keyboard.KEY_D).forEach(dt -> Window2D.viewPos = Window2D.viewPos.add(new Vec2(speed * dt, 0)));

        Core.render.onEvent(Tile::drawAll);
    }

    public static Optional<Tile> tileAt(Vec2 pos) {
        if (pos.containedBy(new Vec2(0), new Vec2(GRID_SIZE * TILE_SIZE - 1))) {
            return Optional.of(grid[(int) (pos.x / TILE_SIZE)][(int) (pos.y / TILE_SIZE)]);
        }
        return Optional.empty();
    }

    public static List<Tile> tilesNear(Vec2 pos, int amt) {
        List<Tile> r = new LinkedList();
        Util.forRange(0, amt, 0, amt, (x, y) -> {
            tileAt(pos.add(new Vec2(x, y).subtract(new Vec2(amt / 2. - .5)).multiply(TILE_SIZE))).ifPresent(r::add);
        });
        return r;
    }
}
