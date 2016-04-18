package map;

import invisibleman.Client;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import static map.Chunk.CHUNK_SIZE;
import util.Color4;
import util.Log;
import util.Mutable;
import util.Util;
import util.Vec3;

public class CubeMap {

    public static final int WIDTH = 100, DEPTH = 100, HEIGHT = 60;
    public static final Vec3 WORLD_SIZE = new Vec3(WIDTH, DEPTH, HEIGHT);
    public static final CubeType[][][] map = new CubeType[WIDTH][DEPTH][HEIGHT];
    public static final Chunk[][][] chunks = new Chunk[WIDTH / CHUNK_SIZE][DEPTH / CHUNK_SIZE][HEIGHT / CHUNK_SIZE];

    static {
        Util.forRange(0, WIDTH / CHUNK_SIZE, 0, DEPTH / CHUNK_SIZE, (x, y) -> Util.forRange(0, HEIGHT / CHUNK_SIZE, z -> {
            chunks[x][y][z] = new Chunk(x * CHUNK_SIZE, y * CHUNK_SIZE, z * CHUNK_SIZE);
        }));
    }

    public static void drawAll() {
        Util.forRange(0, WIDTH / CHUNK_SIZE, 0, DEPTH / CHUNK_SIZE, (x, y) -> Util.forRange(0, HEIGHT / CHUNK_SIZE, z -> {
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
        return chunks[x / CHUNK_SIZE][y / CHUNK_SIZE][z / CHUNK_SIZE];
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

    public static boolean isSolid(Vec3 pos) {
        return getCubeType(pos) != null || getCube(pos) == null;
    }

    public static boolean isSolid(Vec3 pos, Vec3 buf) {
        for (double x = pos.x - buf.x; x <= Math.ceil(pos.x + buf.x); x++) {
            for (double y = pos.y - buf.y; y <= Math.ceil(pos.y + buf.y); y++) {
                for (double z = pos.z - buf.z; z <= Math.ceil(pos.z + buf.z); z++) {
                    if (isSolid(new Vec3(x, y, z))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void load(String fileName) {
        try {
            Util.forRange(0, WIDTH, 0, DEPTH, (x, y) -> Util.forRange(0, HEIGHT, z -> {
                map[x][y][z] = null;
            }));
            Files.readAllLines(Paths.get(fileName)).forEach(s -> {

                if (s.charAt(0) == 'f') {

                    double[] cs = argsGet(s.substring(2), 3);
                    Client.fogColor = new Color4(cs[0], cs[1], cs[2]);
                } else {

                    double[] cs = argsGet(s, 3);
                    s = s.substring(s.lastIndexOf(" ")+1);
                    CubeType ct = s.equals("null") ? null : CubeType.valueOf(s);
                    map[(int) cs[0]][(int) cs[1]][(int) cs[2]] = ct;
                }
            });
            redrawAll();
        } catch (Exception ex) {
            Log.error(ex);
        }
    }

    private static double[] argsGet(String s, int q) {

        double[] ia = new double[q];

        for (int i = 0; i < q; i++) {

            ia[i] = Double.parseDouble(s.substring(0, s.indexOf(" ")));
            s = s.substring(s.indexOf(" ") + 1);
        }
        
        return ia;
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
        Util.forRange(0, WIDTH / CHUNK_SIZE, 0, DEPTH / CHUNK_SIZE, (x, y) -> Util.forRange(0, HEIGHT / CHUNK_SIZE, z -> {
            chunks[x][y][z].redraw();
        }));
    }

    public static void save(String fileName) {
        try {
            PrintWriter writer = new PrintWriter(fileName, "UTF-8");
            writer.printf("f %f %f %f \n",Client.fogColor.r,Client.fogColor.g,Client.fogColor.b);
            Util.forRange(0, WIDTH, 0, DEPTH, (x, y) -> Util.forRange(0, HEIGHT, z -> {
                CubeType ct = map[x][y][z];
                if (ct != null) {
                    writer.println(x + " " + y + " " + z + " " + ct.name());
                }
            }));
            writer.close();
        } catch (Exception ex) {
            Log.error(ex);
        }
    }
}
