package invisibleman;

import graphics.Graphics2D;
import graphics.data.Sprite;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static org.lwjgl.opengl.GL11.*;
import util.*;
import static util.Color4.BLACK;

public class Tile {

    public static final int GRID_SIZE = 50;
    public static double TILE_SIZE = 1;
    public static double HEIGHT_MULT = .2;

    public static Tile[][] grid;

    static {
        grid = new Tile[GRID_SIZE][GRID_SIZE];
        Util.forRange(0, GRID_SIZE, 0, GRID_SIZE, (x, y) -> grid[x][y] = new Tile(x, y));
    }

    public static Stream<Tile> all() {
        return IntStream.range(0, GRID_SIZE).boxed().flatMap(x -> IntStream.range(0, GRID_SIZE).mapToObj(y -> grid[x][y]));
    }

    public static void drawAll3D() {
        Vec3 playerPos = RegisteredEntity.getAll(InvisibleMan.class).get(0).get("position", Vec3.class).get();
        Vec3 nor = new Vec3(0, 0, 1);

        glBegin(GL_QUADS);
        all().filter(t -> t.x != 0 && t.y != 0 && playerPos.subtract(t.pos()).lengthSquared() < 400).forEach(t -> {

            double min = Math.min(Math.min(grid[t.x - 1][t.y].pos().z, grid[t.x - 1][t.y - 1].pos().z), Math.min(grid[t.x][t.y - 1].pos().z, t.pos().z));
            double max = Math.max(Math.max(grid[t.x - 1][t.y].pos().z, grid[t.x - 1][t.y - 1].pos().z), Math.max(grid[t.x][t.y - 1].pos().z, t.pos().z));

            if (max - min > 2) {

                Color4.gray(.5).glColor();
            } else {

                Color4.WHITE.glColor();
            }

            nor.glNormal();
            t.pos().glVertex();
            nor.glNormal();
            grid[t.x - 1][t.y].pos().glVertex();
            nor.glNormal();
            grid[t.x - 1][t.y - 1].pos().glVertex();
            nor.glNormal();
            grid[t.x][t.y - 1].pos().glVertex();
        });
        glEnd();
        BLACK.glColor();
        glBegin(GL_LINES);
        all().filter(t -> t.x != 0 && t.y != 0 && playerPos.subtract(t.pos()).lengthSquared() < 400).forEach(t -> {
            t.pos().add(new Vec3(0, 0, .01)).glVertex();
            grid[t.x - 1][t.y].pos().add(new Vec3(0, 0, .01)).glVertex();
            t.pos().add(new Vec3(0, 0, .01)).glVertex();
            grid[t.x][t.y - 1].pos().add(new Vec3(0, 0, .01)).glVertex();
        });
        glEnd();
    }

    public static double heightAt(Vec3 pos) {
        Tile t = tileAt(pos.toVec2()).orElse(null);
        if (t == null || t.x == GRID_SIZE - 1 || t.y == GRID_SIZE - 1) {
            return -10000.;
        }
        Vec2 o = new Vec2(1).subtract(pos.subtract(t.pos()).toVec2().divide(TILE_SIZE));
        double height = o.x * o.y * t.height;
        height += (1 - o.x) * o.y * grid[t.x + 1][t.y].height;
        height += (1 - o.x) * (1 - o.y) * grid[t.x + 1][t.y + 1].height;
        height += o.x * (1 - o.y) * grid[t.x][t.y + 1].height;
        return height * HEIGHT_MULT;

//        return tileAt(pos.toVec2()).map(t -> t.height * HEIGHT_MULT).orElse(-10000.);
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

    public static Vec2 normalAt(Vec3 pos) {
        Tile t = tileAt(pos.toVec2()).orElse(null);
        if (t == null || t.x == GRID_SIZE - 1 || t.y == GRID_SIZE - 1) {
            return null;
        }
        Vec2 o = new Vec2(1).subtract(pos.subtract(t.pos()).toVec2().divide(TILE_SIZE));
        double xSlope = o.y * (t.height - grid[t.x + 1][t.y].height)
                + (1 - o.y) * (grid[t.x][t.y + 1].height - grid[t.x + 1][t.y + 1].height);
        double ySlope = o.x * (t.height - grid[t.x][t.y + 1].height)
                + (1 - o.x) * (grid[t.x + 1][t.y].height - grid[t.x + 1][t.y + 1].height);
        return new Vec2(xSlope, ySlope).multiply(-HEIGHT_MULT / TILE_SIZE);
    }

    public static Vec2 size() {
        return new Vec2(GRID_SIZE * TILE_SIZE);
    }

    public static Optional<Tile> tileAt(Vec2 pos) {
        if (pos.containedBy(new Vec2(0), size())) {
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
        if (RegisteredEntity.getAll(InvisibleMan.class).get(0).get("position", Vec3.class).get().subtract(pos()).lengthSquared() > 400) {
            return;
        }
        if (x == 0 || y == 0) {
            return;
        }

        glBegin(GL_QUADS);
        Vec3 nor = new Vec3(0, 0, 1);
        nor.glNormal();
        pos().glVertex();
        nor.glNormal();
        grid[x - 1][y].pos().glVertex();
        nor.glNormal();
        grid[x - 1][y - 1].pos().glVertex();
        nor.glNormal();
        grid[x][y - 1].pos().glVertex();
        glEnd();

//        glEnable(GL_TEXTURE_2D);
//        Graphics3D.drawSpriteFast(SpriteContainer.loadSprite("green_pinetree"), pos(), grid[x - 1][y].pos(), grid[x - 1][y - 1].pos(), grid[x][y - 1].pos(), new Vec3(0, 0, 1));
//        if (sprite != null) {
//            sprite.draw(pos(), 0, 0);
//        } else {
//            Graphics3D.fillRect(pos(), new Vec2(TILE_SIZE), 0, 0, WHITE);
//        }
//        if (x > 0) {
//            Graphics3D.fillRect(pos(), new Vec2(TILE_SIZE, (grid[x - 1][y].height - height) * HEIGHT_MULT), Math.PI / 2, Math.PI / 2, WHITE);
//        }
//        if (y > 0) {
//            Graphics3D.fillRect(pos(), new Vec2(TILE_SIZE, (grid[x][y - 1].height - height) * HEIGHT_MULT), Math.PI / 2, 0, WHITE);
//        }
    }

    public Vec3 pos() {
        return new Vec3(x * TILE_SIZE, y * TILE_SIZE, height * HEIGHT_MULT);
    }
}
