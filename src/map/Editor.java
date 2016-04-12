package map;

import com.codepoetics.protonpack.StreamUtils;
import engine.AbstractEntity.LAE;
import engine.Core;
import engine.Input;
import engine.Signal;
import graphics.Camera;
import graphics.Graphics2D;
import graphics.Window3D;
import static graphics.Window3D.*;
import invisibleman.Fog;
import invisibleman.Premade3D;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.Supplier;
import static map.CubeMap.*;
import static map.CubeType.*;
import static org.lwjgl.input.Keyboard.*;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import static util.Color4.*;
import util.*;

public class Editor {

    private static final boolean GENERATE_RANDOM_TERRAIN = false;

    public static void main(String[] args) {

        //Initial graphics setup
        Core.is3D = true;
        Core.init();
        Core.render.bufferCount(Core.interval(1)).forEach(i -> Display.setTitle("FPS: " + i));
        new Fog(new Color4(.95, .8, .3), .00025, 1).create();
        Mouse.setGrabbed(true);

        //Selecting blocks
        Mutable<Integer> selected = new Mutable(0);
        Input.mouseWheel.forEach(x -> selected.o = (selected.o + x / 120 + CubeType.values().length) % CubeType.values().length);
        Input.whenKey(KEY_UP, true).onEvent(() -> selected.o = (selected.o + 1) % CubeType.values().length);
        Input.whenKey(KEY_DOWN, true).onEvent(() -> selected.o = (selected.o - 1 + CubeType.values().length) % CubeType.values().length);

        //Draw world
        Core.render.onEvent(() -> CubeMap.drawAll());

        //Draw GUI
        Core.renderLayer(100).onEvent(() -> {
            Camera.setProjection2D(new Vec2(0), new Vec2(1200, 800));

            Graphics2D.drawEllipse(new Vec2(600, 400), new Vec2(10), RED, 20);

            if (CubeMap.isSolid(pos)) {
                Graphics2D.drawText("Inside Block", new Vec2(542, 350));
            }

            Graphics2D.fillRect(new Vec2(545, 145), new Vec2(110), gray(.5));
            Graphics2D.drawRect(new Vec2(545, 145), new Vec2(110), BLACK);

            Graphics2D.drawSprite(CubeType.values()[selected.o].texture, new Vec2(600, 200), new Vec2(100. / CubeType.values()[selected.o].texture.getImageWidth()), 0, WHITE);

            Graphics2D.drawRect(new Vec2(550, 150), new Vec2(100), BLACK);
            if (CubeMap.isSolid(pos)) {
                Graphics2D.fillRect(new Vec2(0), new Vec2(1200, 800), RED.withA(.4));
            }

            Window3D.resetProjection();
        });

        //Movement
        Premade3D.makeMouseLook(new LAE(e -> {
        }), 2, -1.5, 1.5);
        Signal<Boolean> fast = Input.whenKey(KEY_TAB, true).reduce(false, b -> !b);
        Supplier<Double> speed = fast.map(b -> b ? 30. : 5);
        Input.whileKeyDown(KEY_W).forEach(dt -> pos = pos.add((fast.get() ? facing.toVec3() : forwards()).multiply(speed.get() * dt)));
        Input.whileKeyDown(KEY_S).forEach(dt -> pos = pos.add((fast.get() ? facing.toVec3() : forwards()).multiply(-speed.get() * dt)));
        Input.whileKeyDown(KEY_A).forEach(dt -> pos = pos.add(facing.toVec3().cross(UP).withLength(-speed.get() * dt)));
        Input.whileKeyDown(KEY_D).forEach(dt -> pos = pos.add(facing.toVec3().cross(UP).withLength(speed.get() * dt)));
        Input.whileKeyDown(KEY_SPACE).forEach(dt -> pos = pos.add(UP.multiply(speed.get() * dt)));
        Input.whileKeyDown(KEY_LSHIFT).forEach(dt -> pos = pos.add(UP.multiply(-speed.get() * dt)));

        //Initial level
        if (GENERATE_RANDOM_TERRAIN) {
            HeightGenerator hg = new HeightGenerator(101, 101);
            hg.generate();
            int[][] hm = hg.getMap();
            for (int[] m : hm) {
                for (int j = 0; j < m.length; j++) {
                    m[j] /= 6;
                    m[j] = m[j] > 40 ? 40 : m[j];
                }
            }

            Util.forRange(0, WIDTH, 0, DEPTH, (x, y) -> Util.forRange(0, hm[x][y] < 1 ? 0 : (hm[x][y] - 1), z -> {
                map[x][y][z] = DIRT;
            }));
            Util.forRange(0, WIDTH, 0, DEPTH, (x, y) -> Util.forRange(hm[x][y] < 1 ? 0 : (hm[x][y] - 1), hm[x][y], z -> {
                map[x][y][z] = SNOW;
            }));
            Util.forRange(0, WIDTH, 0, DEPTH, (x, y) -> Util.forRange(hm[x][y], 10, z -> {
                map[x][y][z] = STONE;
            }));
        } else {
            Util.forRange(0, WIDTH, 0, DEPTH, (x, y) -> Util.forRange(0, 10, z -> {
                map[x][y][z] = SNOW;
            }));
        }
        CubeMap.redrawAll();
        pos = WORLD_SIZE.multiply(.5);

        //Fill tool
        Mutable<CubeData> toFill = new Mutable(null);
        Input.whenKey(KEY_F, true).onEvent(() -> {
            if (toFill.o != null) {
                toFill.o = null;
            } else {
                CubeMap.rayCastStream(pos, facing.toVec3()).filter(cd -> cd.c != null).findFirst().ifPresent(cd -> {
                    toFill.o = cd;
                });
            }
        });

        //Destroy blocks
        Input.whenMouse(0, true).filter(() -> !isSolid(pos)).onEvent(() -> {
            if (toFill.o != null) {
                CubeMap.rayCastStream(pos, facing.toVec3()).filter(cd -> cd.c != null).findFirst().ifPresent(cd -> {
                    Util.forRange(Math.min(cd.x, toFill.o.x), Math.max(cd.x, toFill.o.x) + 1, Math.min(cd.y, toFill.o.y), Math.max(cd.y, toFill.o.y) + 1,
                            (x, y) -> Util.forRange(Math.min(cd.z, toFill.o.z), Math.max(cd.z, toFill.o.z) + 1, z -> {
                                CubeMap.map[x][y][z] = null;
                            }));
                    CubeMap.redrawAll();
                });
            } else {
                CubeMap.rayCastStream(pos, facing.toVec3()).filter(cd -> cd.c != null).findFirst().ifPresent(cd -> {
                    CubeMap.map[cd.x][cd.y][cd.z] = null;
                    CubeMap.redraw(new Vec3(cd.x, cd.y, cd.z));
                });
            }
            toFill.o = null;
        });

        //Place blocks
        Input.whenMouse(1, true).filter(() -> !isSolid(pos)).onEvent(() -> {
            if (toFill.o != null) {
                CubeMap.rayCastStream(pos, facing.toVec3()).filter(cd -> cd.c != null).findFirst().ifPresent(cd -> {
                    Util.forRange(Math.min(cd.x, toFill.o.x), Math.max(cd.x, toFill.o.x) + 1, Math.min(cd.y, toFill.o.y), Math.max(cd.y, toFill.o.y) + 1,
                            (x, y) -> Util.forRange(Math.min(cd.z, toFill.o.z), Math.max(cd.z, toFill.o.z) + 1, z -> {
                                CubeMap.map[x][y][z] = CubeType.values()[selected.o];
                            }));
                    CubeMap.redrawAll();
                });
            } else {
                StreamUtils.takeWhile(CubeMap.rayCastStream(pos, facing.toVec3()).skip(1), cd -> cd.c == null).reduce((a, b) -> b).ifPresent(cd -> {
                    CubeMap.map[cd.x][cd.y][cd.z] = CubeType.values()[selected.o];
                    CubeMap.redraw(new Vec3(cd.x, cd.y, cd.z));
                });
            }
            toFill.o = null;
        });

        //Repaint blocks
        Input.whileMouse(2, true).onEvent(() -> {
            CubeMap.rayCastStream(pos, facing.toVec3()).filter(cd -> cd.c != null).findFirst().ifPresent(cd -> {
                CubeMap.map[cd.x][cd.y][cd.z] = CubeType.values()[selected.o];
                CubeMap.redraw(new Vec3(cd.x, cd.y, cd.z));
            });
            toFill.o = null;
        });

        //Fill extra terrain
        Input.whenKey(KEY_EQUALS, true).onEvent(() -> {
            boolean[][][] checked = new boolean[WIDTH][DEPTH][HEIGHT];
            Util.forRange(0, WIDTH, 0, DEPTH, (x, y) -> Util.forRange(0, HEIGHT, z -> {
                checked[x][y][z] = isSolid(new Vec3(x, y, z));
            }));
            Queue<CubeData> toCheck = new LinkedList();
            toCheck.add(getCube(WORLD_SIZE.multiply(.5)));
            while (!toCheck.isEmpty()) {
                CubeData cd = toCheck.poll();
                if (checked[cd.x][cd.y][cd.z]) {
                    continue;
                }
                checked[cd.x][cd.y][cd.z] = true;
                List<CubeData> neighbors = new ArrayList();
                neighbors.addAll(Arrays.asList(getCube(new Vec3(cd.x + 1, cd.y, cd.z)), getCube(new Vec3(cd.x - 1, cd.y, cd.z)),
                        getCube(new Vec3(cd.x, cd.y + 1, cd.z)), getCube(new Vec3(cd.x, cd.y - 1, cd.z)),
                        getCube(new Vec3(cd.x, cd.y, cd.z + 1)), getCube(new Vec3(cd.x, cd.y, cd.z - 1))
                ));
                neighbors.removeIf(c -> c == null);
                neighbors.forEach(n -> {
                    if (!checked[n.x][n.y][n.z]) {
                        toCheck.add(n);
                    }
                });
            };
            Util.forRange(0, WIDTH, 0, DEPTH, (x, y) -> Util.forRange(0, HEIGHT, z -> {
                if (!checked[x][y][z]) {
                    map[x][y][z] = SAND;
                }
            }));
            CubeMap.redrawAll();
        });

        //Save and load
        Input.whenKey(KEY_RETURN, true).combineEventStreams(Core.interval(60)).onEvent(() -> {
            try {
                PrintWriter writer = new PrintWriter("autosaves/level" + System.currentTimeMillis() + ".txt", "UTF-8");
                Util.forRange(0, WIDTH, 0, DEPTH, (x, y) -> Util.forRange(0, HEIGHT, z -> {
                    CubeType ct = map[x][y][z];
                    writer.println(x + " " + y + " " + z + " " + (ct == null ? "null" : ct.name()));
                }));
                writer.close();
            } catch (Exception ex) {
                Log.error(ex);
            }
            try {
                PrintWriter writer = new PrintWriter("level3.txt", "UTF-8");
                Util.forRange(0, WIDTH, 0, DEPTH, (x, y) -> Util.forRange(0, HEIGHT, z -> {
                    CubeType ct = map[x][y][z];
                    writer.println(x + " " + y + " " + z + " " + (ct == null ? "null" : ct.name()));
                }));
                writer.close();
            } catch (Exception ex) {
                Log.error(ex);
            }
        });
        Input.whenKey(KEY_L, true).onEvent(() -> CubeMap.load("level3.txt"));

        Core.run();
    }
}
