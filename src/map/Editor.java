package map;

import com.codepoetics.protonpack.StreamUtils;
import engine.AbstractEntity.LAE;
import engine.Core;
import engine.Input;
import engine.Signal;
import game.Fog;
import game.Premade3D;
import graphics.Camera;
import graphics.Graphics2D;
import graphics.Window3D;
import static graphics.Window3D.*;
import graphics.data.Texture;
import java.util.*;
import java.util.function.Supplier;
import static map.CubeMap.*;
import static map.CubeType.*;
import networking.Client;
import static networking.Client.sendMessage;
import static networking.MessageType.BLOCK_PLACE;
import static networking.MessageType.MODEL_PLACE;
import static org.lwjgl.input.Keyboard.*;
import org.lwjgl.input.Mouse;
import static util.Color4.*;
import util.Mutable;
import util.Util;
import util.Vec2;
import util.Vec3;

public class Editor {

    private static final boolean GENERATE_RANDOM_TERRAIN = false;
    private static final boolean IS_MULTIPLAYER = false;
    public static boolean model = false;

    public static void start(String mapname, String ip) {
        Client.connect(ip);

        //Hide the mouse
        Mouse.setGrabbed(true);

        new Fog(BLACK, .00025, 1).create();

        //Selecting blocks
        Mutable<Integer> selected = new Mutable(0);
        Mutable<Integer> selmodel = new Mutable(0);

        Input.mouseWheel.forEach(x -> {
            if (model) {
                selmodel.o = (selmodel.o + x / 120 + ModelList.getAll().size()) % ModelList.getAll().size();
            } else {
                selected.o = (selected.o + x / 120 + CubeType.distinct()) % CubeType.distinct();
            }
        });
        Input.whenKey(KEY_UP, true).onEvent(() -> {
            if (model) {
                selmodel.o = (selmodel.o + 1) % ModelList.getAll().size();
            } else {
                selected.o = (selected.o + 1) % CubeType.distinct();
            }
        });
        Input.whenKey(KEY_DOWN, true).onEvent(() -> {
            if (model) {
                selmodel.o = (selmodel.o - 1 + ModelList.getAll().size()) % ModelList.getAll().size();
            } else {
                selected.o = (selected.o - 1 + CubeType.distinct()) % CubeType.distinct();
            }
        });

        Input.whenKey(KEY_M, true).onEvent(() -> {
            //model = !model;
        });
        //Initial level
        if (!IS_MULTIPLAYER) {
            if (GENERATE_RANDOM_TERRAIN) {
                HeightGenerator hg = new HeightGenerator(101, 101);
                hg.generate();
                int[][] hm = hg.getMap();
                for (int[] m : hm) {
                    for (int j = 0; j < m.length; j++) {
                        m[j] /= 18;
                        m[j] += 4;
                        m[j] = m[j] > 40 ? 40 : m[j];
                    }
                }

                Util.forRange(0, WIDTH, 0, DEPTH, (x, y) -> Util.forRange(0, hm[x][y] < 1 ? 0 : (hm[x][y] - 1), z -> {
                    setCube(x, y, z, CubeType.getByName("sand_white"));
                }));
                Util.forRange(0, WIDTH, 0, DEPTH, (x, y) -> Util.forRange(hm[x][y] < 1 ? 0 : (hm[x][y] - 1), hm[x][y], z -> {
                    setCube(x, y, z, CubeType.getByName("sand_white"));
                }));
                Util.forRange(0, WIDTH, 0, DEPTH, (x, y) -> Util.forRange(hm[x][y], 10, z -> {
                    setCube(x, y, z, getRandom(getGroup(getByName("water_1"))));
                }));
            } else {
//                Util.forRange(0, WIDTH, 0, DEPTH, (x, y) -> Util.forRange(0, 10, z -> {
//                    map[x][y][z] = SNOW;
//                }));
                load("levels/level_" + mapname + ".txt");
            }
        }

        //Draw world
        Core.render.onEvent(() -> drawAll());

        //Draw GUI
        Core.renderLayer(100).onEvent(() -> {
            Camera.setProjection2D(new Vec2(0), new Vec2(1200, 800));

            Graphics2D.drawEllipse(new Vec2(600, 400), new Vec2(10), RED, 20);

//            Graphics2D.fillRect(new Vec2(545, 145), new Vec2(110), gray(.5));
//            Graphics2D.drawRect(new Vec2(545, 145), new Vec2(110), BLACK);
//
//            Graphics2D.drawSprite(CubeType.getFirst(selected.o).texture, new Vec2(600, 200), new Vec2(100. / CubeType.getFirst(selected.o).texture.getImageWidth()), 0, WHITE);
//            Graphics2D.drawRect(new Vec2(550, 150), new Vec2(100), BLACK);
            for (int i = -8; i < 0; i++) {
                if (model) {
                    drawIcon(new Vec2(580 + .25 * (349 + 11 * i) * i, 89 - 2.75 * Math.abs(i)), 80 - 5 * Math.abs(i), (ModelList.getAll().size() - (i + selmodel.o)) % ModelList.getAll().size());
                } else {
                    CubeType.getFirst((selected.o + i + CubeType.distinct()) % CubeType.distinct()).draw(new Vec2(580 + .25 * (349 + 11 * i) * i, 89 - 2.75 * Math.abs(i)), 80 - 5 * Math.abs(i));
                }
            }
            for (int i = 8; i > 0; i--) {
                if (model) {
                    drawIcon(new Vec2(620 + .25 * (349 - 11 * i) * i, 89 - 2.75 * Math.abs(i)), 80 - 5 * Math.abs(i), (i + selmodel.o) % ModelList.getAll().size());
                } else {
                    CubeType.getFirst((selected.o + i + CubeType.distinct()) % CubeType.distinct()).draw(new Vec2(620 + .25 * (349 - 11 * i) * i, 89 - 2.75 * Math.abs(i)), 80 - 5 * Math.abs(i));
                }
            }
            if (model) {
                drawIcon(new Vec2(600, 100), 100, selmodel.o);
            } else {
                CubeType.getFirst(selected.o).draw(new Vec2(600, 100), 100);
            }

            if (isSolid(pos)) {
                Graphics2D.fillRect(new Vec2(0), new Vec2(1200, 800), RED.withA(.4));
                Graphics2D.drawText("Inside Block", new Vec2(542, 350));
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

        Input.whenKey(KEY_R, true).filter(() -> model).onEvent(() -> {
            ModelList.removeAll();
            ModelList.drawAll();
        });

        pos = WORLD_SIZE.multiply(.5);

        //Fill tool
        Mutable<CubeData> toFill = new Mutable(null);
        Input.whenKey(KEY_F, true).onEvent(() -> {
            if (toFill.o != null) {
                toFill.o = null;
            } else {
                rayCastStream(pos, facing.toVec3()).filter(cd -> cd.c != null).findFirst().ifPresent(cd -> {
                    toFill.o = cd;
                });
            }
        });

        //Destroy blocks
        Input.whenMouse(0, true).filter(() -> !isSolid(pos)).onEvent(() -> {
            if (model) {
                StreamUtils.takeWhile(rayCastStream(pos, facing.toVec3()).skip(1), cd -> cd.c == null).reduce((a, b) -> b).ifPresent(cd -> {
                    ModelList.remove(new Vec3(cd.x, cd.y, cd.z));
                    sendMessage(MODEL_PLACE, new Vec3(cd.x, cd.y, cd.z), null);
                });
            } else if (toFill.o != null) {
                rayCastStream(pos, facing.toVec3()).filter(cd -> cd.c != null).findFirst().ifPresent(cd -> {
                    Util.forRange(Math.min(cd.x, toFill.o.x), Math.max(cd.x, toFill.o.x) + 1, Math.min(cd.y, toFill.o.y), Math.max(cd.y, toFill.o.y) + 1,
                            (x, y) -> Util.forRange(Math.min(cd.z, toFill.o.z), Math.max(cd.z, toFill.o.z) + 1, z -> {
                                setCube(x, y, z, null);
                                sendMessage(BLOCK_PLACE, new Vec3(x, y, z), -1);
                            }));
                });
            } else {
                rayCastStream(pos, facing.toVec3()).filter(cd -> cd.c != null).findFirst().ifPresent(cd -> {
                    setCube(cd.x, cd.y, cd.z, null);
                    sendMessage(BLOCK_PLACE, new Vec3(cd.x, cd.y, cd.z), -1);
                });
            }
            toFill.o = null;
        });

        //Place blocks
        Input.whenMouse(1, true).filter(() -> !isSolid(pos)).onEvent(() -> {
            if (model) {
                StreamUtils.takeWhile(rayCastStream(pos, facing.toVec3()).skip(1), cd -> cd.c == null).reduce((a, b) -> b).ifPresent(cd -> {
                    ModelList.add(new Vec3(cd.x, cd.y, cd.z), selmodel.o);
                    sendMessage(MODEL_PLACE, new Vec3(cd.x, cd.y, cd.z), selmodel.o);
                });
            } else if (toFill.o != null) {
                rayCastStream(pos, facing.toVec3()).filter(cd -> cd.c != null).findFirst().ifPresent(cd -> {
                    Util.forRange(Math.min(cd.x, toFill.o.x), Math.max(cd.x, toFill.o.x) + 1, Math.min(cd.y, toFill.o.y), Math.max(cd.y, toFill.o.y) + 1,
                            (x, y) -> Util.forRange(Math.min(cd.z, toFill.o.z), Math.max(cd.z, toFill.o.z) + 1, z -> {
                                CubeType ct = CubeType.getRandom(selected.o);
                                setCube(x, y, z, ct);
                                sendMessage(BLOCK_PLACE, new Vec3(x, y, z), ct.id);
                            }));
                });
            } else {
                StreamUtils.takeWhile(rayCastStream(pos, facing.toVec3()).skip(1), cd -> cd.c == null).reduce((a, b) -> b).ifPresent(cd -> {
                    CubeType ct = CubeType.getRandom(selected.o);
                    setCube(cd.x, cd.y, cd.z, ct);
                    sendMessage(BLOCK_PLACE, new Vec3(cd.x, cd.y, cd.z), ct.id);
                });
            }
            toFill.o = null;
        });

        //Repaint blocks
        Input.whileMouse(2, true).onEvent(() -> {
            rayCastStream(pos, facing.toVec3()).filter(cd -> cd.c != null).findFirst().ifPresent(cd -> {
                CubeType ct = CubeType.getRandom(selected.o);
                setCube(cd.x, cd.y, cd.z, ct);
                sendMessage(BLOCK_PLACE, new Vec3(cd.x, cd.y, cd.z), ct.id);
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
            }
            Util.forRange(0, WIDTH, 0, DEPTH, (x, y) -> Util.forRange(0, HEIGHT, z -> {
                if (!checked[x][y][z]) {
                    setCube(x, y, z, CubeType.getByName("snow"));
                    sendMessage(BLOCK_PLACE, new Vec3(x, y, z), CubeType.getByName("snow").id);
                }
            }));
        });

        //Randomize blocks
        Input.whenKey(KEY_RBRACKET, true).onEvent(() -> {
            Util.forRange(0, WIDTH, 0, DEPTH, (x, y) -> Util.forRange(0, HEIGHT, z -> {
                if (getCubeType(x, y, z) != null) {
                    if (CubeType.hasVersions(CubeType.getGroup(getCubeType(x, y, z)))) {
                        setCube(x, y, z, CubeType.getRandom(CubeType.getGroup(getCubeType(x, y, z))));
                        sendMessage(BLOCK_PLACE, new Vec3(x, y, z), getCubeType(x, y, z).id);
                    }
                }
            }));
        });

        //Select block
        Input.whenKey(KEY_Q, true).onEvent(() -> {
            rayCastStream(pos, facing.toVec3()).filter(cd -> cd.c != null).findFirst().ifPresent(cd -> {
                selected.o = CubeType.getGroup(cd.c);
            });
        });

        //Save and load
        Input.whenKey(KEY_RETURN, true).combineEventStreams(Core.interval(60)).onEvent(() -> {
            save("levels/autosaves/level_" + MAP_NAME + "_" + System.currentTimeMillis() + ".txt");
            save("levels/level_" + MAP_NAME + ".txt");
        });
        //Input.whenKey(KEY_L, true).onEvent(() -> load("levels/level_" + mapname + ".txt"));

        //Sync with other clients
        Input.whenKey(KEY_MINUS, true).onEvent(() -> {
            Util.forRange(0, WIDTH, 0, DEPTH, (x, y) -> Util.forRange(0, HEIGHT, z -> {
                sendMessage(BLOCK_PLACE, new Vec3(x, y, z), getCubeType(x, y, z).id);
            }));
        });

        Core.run();
    }

    private static void drawIcon(Vec2 pos, double size, int id) {
        Texture t = ModelList.getIcon(id);

        Graphics2D.fillRect(pos.subtract(new Vec2(size * .55)), new Vec2(size * 1.1), gray(.5));
        Graphics2D.drawRect(pos.subtract(new Vec2(size * .55)), new Vec2(size * 1.1), BLACK);

        Graphics2D.drawSprite(t, pos, new Vec2(size / t.getImageWidth()), 0, WHITE);
        Graphics2D.drawRect(pos.subtract(new Vec2(size * .5)), new Vec2(size), BLACK);

    }
}
