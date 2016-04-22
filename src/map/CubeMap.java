package map;

import game.Fog;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;
import static java.util.stream.Stream.of;
import java.util.stream.StreamSupport;
import static map.Chunk.CHUNK_SIZE;
import util.*;

public class CubeMap {

    public static final int WIDTH = 100, DEPTH = 100, HEIGHT = 60;
    public static final Vec3 WORLD_SIZE = new Vec3(WIDTH, DEPTH, HEIGHT);

    private static final CubeType[][][] MAP = new CubeType[WIDTH][DEPTH][HEIGHT];
    private static final Chunk[][][] CHUNKS = new Chunk[WIDTH / CHUNK_SIZE][DEPTH / CHUNK_SIZE][HEIGHT / CHUNK_SIZE];

    private static final Set<Chunk> TO_REDRAW = new HashSet();

    static {
        Util.forRange(0, WIDTH / CHUNK_SIZE, 0, DEPTH / CHUNK_SIZE, (x, y) -> Util.forRange(0, HEIGHT / CHUNK_SIZE, z -> {
            CHUNKS[x][y][z] = new Chunk(x * CHUNK_SIZE, y * CHUNK_SIZE, z * CHUNK_SIZE);
            TO_REDRAW.add(CHUNKS[x][y][z]);
        }));
    }

    public static void drawAll() {
        TO_REDRAW.stream().filter(c -> c != null).forEach(Chunk::redraw);
        TO_REDRAW.clear();
        Util.forRange(0, WIDTH / CHUNK_SIZE, 0, DEPTH / CHUNK_SIZE, (x, y) -> Util.forRange(0, HEIGHT / CHUNK_SIZE, z -> {
            CHUNKS[x][y][z].draw();
        }));
        ModelList.drawAll();
    }

    public static Chunk getChunk(Vec3 pos) {
        pos = pos.perComponent(Math::floor);
        int x = (int) pos.x;
        int y = (int) pos.y;
        int z = (int) pos.z;
        if (x < 0 || x >= WIDTH || y < 0 || y >= DEPTH || z < 0 || z >= HEIGHT) {
            return null;
        }
        return CHUNKS[x / CHUNK_SIZE][y / CHUNK_SIZE][z / CHUNK_SIZE];
    }

    public static CubeData getCube(Vec3 pos) {
        pos = pos.perComponent(Math::floor);
        int x = (int) pos.x;
        int y = (int) pos.y;
        int z = (int) pos.z;
        if (x < 0 || x >= WIDTH || y < 0 || y >= DEPTH || z < 0 || z >= HEIGHT) {
            return null;
        }
        return new CubeData(x, y, z, MAP[x][y][z]);
    }

    public static CubeType getCubeType(Vec3 pos) {
        CubeData cd = getCube(pos);
        return cd == null ? null : cd.c;
    }

    public static CubeType getCubeType(int x, int y, int z) {
        return MAP[x][y][z];
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
            CubeType.getAll();
            Util.forRange(0, WIDTH, 0, DEPTH, (x, y) -> Util.forRange(0, HEIGHT, z -> {
                setCube(x, y, z, null);
            }));

            Map<String, String> replace = new HashMap();

            Files.readAllLines(Paths.get(fileName)).forEach(s -> {

                if (s.startsWith("f")) {
                    double[] cs = argsGet(s.substring(2), 3);
                    Fog.setFogColor(new Color4(cs[0], cs[1], cs[2]));
                    System.out.println("fog set");
                }
                else if (s.startsWith("m")) {
                    double[] cs = argsGet(s.substring(2),3);
                    ModelList.add(new Vec3(cs[0],cs[1],cs[2]),s.substring(s.lastIndexOf(" ")+1));
                    ModelList.draw(new Vec3(cs[0],cs[1],cs[2]));
                }
                else {
                    double[] cs = argsGet(s, 3);
                    s = s.substring(s.lastIndexOf(" ") + 1).toLowerCase();

                    CubeType ct = CubeType.getByName(s);//s.equals("null") ? null : CubeType.valueOf(s);

                    if (ct == null) {
                        if (!replace.containsKey(s)) {
                            System.out.println("Unknown block type: " + s);
                            System.out.println("Please enter the replacement name:");
                            Scanner in = new Scanner(System.in);
                            String n = in.next();
                            replace.put(s, n);
                        }
                        ct = CubeType.getByName(replace.get(s));
                    }

                    setCube((int) cs[0], (int) cs[1], (int) cs[2], ct);
                }
            });
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

    public static void save(String fileName) {
        try {
            PrintWriter writer = new PrintWriter(fileName, "UTF-8");
            writer.printf("f %f %f %f \n", Fog.FOG_COLOR.r, Fog.FOG_COLOR.g, Fog.FOG_COLOR.b);
            Util.forRange(0, WIDTH, 0, DEPTH, (x, y) -> Util.forRange(0, HEIGHT, z -> {
                CubeType ct = MAP[x][y][z];
                if (ct != null) {
                    writer.println(x + " " + y + " " + z + " " + ct.name);
                }
            }));
            ModelList.save(writer);
            writer.close();
        } catch (Exception ex) {
            Log.error(ex);
        }
    }

    public static void setCube(int x, int y, int z, CubeType ct) {
        if (MAP[x][y][z] != ct) {
            MAP[x][y][z] = ct;
            Vec3 pos = new Vec3(x, y, z);
            of(-1, 1).forEach(x2 -> of(-1, 1).forEach(y2 -> of(-1, 1).forEach(z2 -> TO_REDRAW.add(getChunk(pos.add(new Vec3(x2, y2, z2)))))));
        }
    }
}
