package mc_leveleditor;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import static mc_leveleditor.Chunk.SIZE;
import util.Log;
import util.Mutable;
import util.Util;
import util.Vec3;

public class CubeMap {

    public static final int WIDTH = 100, DEPTH = 100, HEIGHT = 40;
    public static final CubeType[][][] map = new CubeType[WIDTH][DEPTH][HEIGHT];
    public static final Chunk[][][] chunks = new Chunk[WIDTH / SIZE][DEPTH / SIZE][HEIGHT / SIZE];

    static {
        Util.forRange(0, WIDTH / SIZE, 0, DEPTH / SIZE, (x, y) -> Util.forRange(0, HEIGHT / SIZE, z -> {
            chunks[x][y][z] = new Chunk(x * SIZE, y * SIZE, z * SIZE);
        }));
    }

    public static void drawAll() {
        Util.forRange(0, WIDTH / SIZE, 0, DEPTH / SIZE, (x, y) -> Util.forRange(0, HEIGHT / SIZE, z -> {
            chunks[x][y][z].draw();
        }));
    }

    public static Chunk getChunk(Vec3 pos) {
        pos = pos.perComponent(Math::floor);
        int x = (int) pos.x;
        int y = (int) pos.y;
        int z = (int) pos.z;
        if (x < 0 || x >= WIDTH || y < 0 || y >= DEPTH || z < 0 || z >= HEIGHT) {
            return null;
        }
        return chunks[x / SIZE][y / SIZE][z / SIZE];
    }

    public static CubeData getCube(Vec3 pos) {
        pos = pos.perComponent(Math::floor);
        int x = (int) pos.x;
        int y = (int) pos.y;
        int z = (int) pos.z;
        if (x < 0 || x >= WIDTH || y < 0 || y >= DEPTH || z < 0 || z >= HEIGHT) {
            return null;
        }
        return new CubeData(x, y, z, map[x][y][z]);
    }

    public static CubeType getCubeType(Vec3 pos) {
        CubeData cd = getCube(pos);
        return cd == null ? null : cd.c;
    }

    public static void load(String fileName) {
        try {
            Files.readAllLines(Paths.get(fileName)).forEach(s -> {
                int x = Integer.parseInt(s.substring(0, s.indexOf(" ")));
                s = s.substring(s.indexOf(" ") + 1);
                int y = Integer.parseInt(s.substring(0, s.indexOf(" ")));
                s = s.substring(s.indexOf(" ") + 1);
                int z = Integer.parseInt(s.substring(0, s.indexOf(" ")));
                s = s.substring(s.indexOf(" ") + 1);
                CubeType ct = s.equals("null") ? null : CubeType.valueOf(s);
                map[x][y][z] = ct;
            });
            redrawAll();
        } catch (Exception ex) {
            Log.error(ex);
        }
    }

    public static Iterable<CubeData> rayCast(Vec3 pos, Vec3 dir) {
        BinaryOperator<Double> timeToEdge = (x, d) -> (d < 0) ? -x / d : (1 - x) / d;
        return () -> {
            Mutable<Vec3> cp = new Mutable(pos);
            return new Iterator() {
                @Override
                public boolean hasNext() {
                    return getCube(cp.o) != null;
                }

                @Override
                public Object next() {
                    CubeData c = getCube(cp.o);

                    Vec3 relPos = cp.o.perComponent(d -> d % 1);
                    Vec3 time = relPos.perComponent(dir, timeToEdge);
                    double minTime = .001 + Math.min(time.x, Math.min(time.y, time.z));
                    cp.o = cp.o.add(dir.multiply(minTime));

                    return c;
                }
            };
        };
    }

    public static Stream<CubeData> rayCastStream(Vec3 pos, Vec3 dir) {
        return StreamSupport.stream(rayCast(pos, dir).spliterator(), false);
    }

    public static void redraw(Vec3 pos) {
        Set<Chunk> toRedraw = new HashSet();
        toRedraw.add(getChunk(pos));
        toRedraw.add(getChunk(pos.add(new Vec3(1, 0, 0))));
        toRedraw.add(getChunk(pos.add(new Vec3(-1, 0, 0))));
        toRedraw.add(getChunk(pos.add(new Vec3(0, 1, 0))));
        toRedraw.add(getChunk(pos.add(new Vec3(0, -1, 0))));
        toRedraw.add(getChunk(pos.add(new Vec3(0, 0, 1))));
        toRedraw.add(getChunk(pos.add(new Vec3(0, 0, -1))));
        toRedraw.removeIf(c -> c == null);
        toRedraw.forEach(Chunk::redraw);
    }

    public static void redrawAll() {
        Util.forRange(0, WIDTH / SIZE, 0, DEPTH / SIZE, (x, y) -> Util.forRange(0, HEIGHT / SIZE, z -> {
            chunks[x][y][z].redraw();
        }));
    }
}