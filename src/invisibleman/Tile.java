package invisibleman;

import graphics.Graphics2D;
import graphics.Graphics3D;
import graphics.data.Sprite;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static util.Color4.BLACK;
import static util.Color4.WHITE;
import util.*;

public class Tile {

    public static final int GRID_SIZE = 50;
    public static double TILE_SIZE = 1;
    public static double HEIGHT_MULT = .1;

    public static Tile[][] grid;

    static {
        grid = new Tile[GRID_SIZE][GRID_SIZE];
        Util.forRange(0, GRID_SIZE, 0, GRID_SIZE, (x, y) -> grid[x][y] = new Tile(x, y));
    }

    public static Stream<Tile> all() {
        return IntStream.range(0, GRID_SIZE).boxed().flatMap(x -> IntStream.range(0, GRID_SIZE).mapToObj(y -> grid[x][y]));
    }

    public static double heightAt(Vec3 pos) {
        return tileAt(pos.toVec2()).get().height * HEIGHT_MULT;
    }

    public static void load(String file) {
        try {
            Files.readAllLines(Paths.get(file)).forEach(s -> {
                int x = Integer.parseInt(s.substring(0, s.indexOf(" ")));
                s = s.substring(s.indexOf(" ") + 1);
                int y = Integer.parseInt(s.substring(0, s.indexOf(" ")));
                s = s.substring(s.indexOf(" ") + 1);
                double height = Double.parseDouble(s.substring(0, s.indexOf(" ")));
                s = s.substring(s.indexOf(" ") + 1);
                Sprite sprite = s.isEmpty() ? null : new Sprite(s);

                Tile t = Tile.grid[x][y];
                t.height = height;
                t.sprite = sprite;
            });
        } catch (Exception ex) {
            Log.error(ex);
        }
    }

    public static Vec2 size() {
        return new Vec2(GRID_SIZE * TILE_SIZE);
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

    public int x, y;
    public double height;
    public Sprite sprite;

    public Tile(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Color4 color() {
        return Color4.gray(height / 64 + .5);
    }

    public void draw2D() {
        if (sprite != null) {
            sprite.color = color();
            sprite.draw(new Vec2(x, y).multiply(TILE_SIZE), 0);
        } else {
            Graphics2D.fillRect(new Vec2(x, y).multiply(TILE_SIZE), new Vec2(TILE_SIZE), color());
        }
        Graphics2D.drawRect(new Vec2(x, y).multiply(TILE_SIZE), new Vec2(TILE_SIZE), BLACK);
    }

    public void draw3D() {
        if (sprite != null) {
            sprite.draw(pos(), 0, 0);
        } else {
            Graphics3D.fillRect(pos(), new Vec2(TILE_SIZE), 0, 0, WHITE);
        }
        if (x > 0) {
            Graphics3D.fillRect(pos(), new Vec2(TILE_SIZE, (grid[x - 1][y].height - height) * HEIGHT_MULT), Math.PI / 2, Math.PI / 2, WHITE);
        }
        if (y > 0) {
            Graphics3D.fillRect(pos(), new Vec2(TILE_SIZE, (grid[x][y - 1].height - height) * HEIGHT_MULT), Math.PI / 2, 0, WHITE);
        }
    }

    public Vec3 pos() {
        return new Vec3(x * TILE_SIZE, y * TILE_SIZE, height * HEIGHT_MULT);
    }
}
