package map;

import graphics.Graphics3D;
import graphics.data.Texture;
import graphics.loading.SpriteContainer;
import java.util.Arrays;
import static java.util.stream.Stream.of;
import static map.CubeMap.getCubeType;
import util.Util;
import util.Vec2;
import util.Vec3;

public enum CubeType {

    SNOW("white_pixel"),
    GRASS("grass"),
    STONE_BRICK("stone_brick3"),
    STONE("stone"),
    DIRT("dirt"),
    SAND("sand"),
    SAND_BRICK("sand_brick"),
    WOOD1("wood001"),
    WOOD2("wood002"),
    WOOD3("wood003"),
    WOOD4("wood004"),
    WOOD5("wood005"),
    WOOD6("wood006"),
    WOOD7("wood007"),
    WOOD8("wood008"),
    WOOD1_B("wood_b1"),
    WOOD2_B("wood_b2"),
    WOOD3_B("wood_b3"),
    WOOD4_B("wood_b4"),
    WOOD5_B("wood_b5"),
    WOOD6_B("wood_b6"),
    WOOD7_B("wood_b7"),
    WOOD8_B("wood_b8"),
    WATER1("water001"),
    WATER2("water002"),
    WATER3("water003"),
    WATER4("water004"),
    LAVA1("lava001"),
    LAVA2("lava002"), 
    LAVA3("lava003"),
    LAVA4("lava004"),;
    public final Texture texture;

    private CubeType(String name) {
        texture = SpriteContainer.loadSprite(name);
    }

    public static CubeType idToType(int id) {
        return id == -1 ? null : values()[id];
    }

    public static int typeToId(CubeType type) {
        return type == null ? -1 : Arrays.asList(values()).indexOf(type);
    }

    public static boolean DRAW_EDGES = true;

    public static void drawEdges(Vec3 pos) {
        if (CubeMap.isSolid(pos)) {
            return;
        }

        double d = 0.01;
        Util.repeat(3, coord -> {
            Vec3 dir = new Vec3(coord == 0 ? 1 : 0, coord == 1 ? 1 : 0, coord == 2 ? 1 : 0);
            of(false, true).forEach(oc1 -> of(false, true).forEach(oc2 -> {
                double[] sp = new double[3];
                boolean c2 = false;
                for (int cc = 0; cc < 3; cc++) {
                    if (cc != coord) {
                        sp[cc] = (c2 ? oc2 : oc1) ? 1 - d : d;
                        c2 = true;
                    }
                }
                Vec3 relPos = new Vec3(sp[0], sp[1], sp[2]);

                Vec3 checkRel = relPos.perComponent(x -> x == 0 ? 0 : x == d ? -1. : 1);
                int count = (int) of(checkRel.withX(0), checkRel.withY(0), checkRel.withZ(0)).filter(v -> !v.equals(checkRel))
                        .filter(v -> CubeMap.isSolid(pos.add(v))).count();
                if (count == 1 || (count == 0 && !CubeMap.isSolid(pos.add(checkRel)))) {
                    return;
                }

                pos.add(relPos).add(dir.multiply(CubeMap.isSolid(pos.subtract(dir)) ? d : -d)).glVertex();
                pos.add(relPos).add(dir.multiply(CubeMap.isSolid(pos.add(dir)) ? 1 - d : 1 + d)).glVertex();
            }));
        });
    }

    public static void drawFaces(Vec3 pos) {
        double delta = 0;
        if (getCubeType(pos.add(new Vec3(0, 0, -1))) == null) {
            Graphics3D.drawQuadFast(pos.add(new Vec3(delta)), new Vec2(1 - 2 * delta), 0, 0);
        }
        if (getCubeType(pos.add(new Vec3(0, 0, 1))) == null) {
            Graphics3D.drawQuadFast(pos.add(new Vec3(1 - delta)), new Vec2(1 - 2 * delta), 0, Math.PI);
        }

        if (getCubeType(pos.add(new Vec3(-1, 0, 0))) == null) {
            Graphics3D.drawQuadFast(pos.add(new Vec3(delta)), new Vec2(1 - 2 * delta), Math.PI / 2, Math.PI / 2);
        }
        if (getCubeType(pos.add(new Vec3(1, 0, 0))) == null) {
            Graphics3D.drawQuadFast(pos.add(new Vec3(1 - delta)), new Vec2(1 - 2 * delta), -Math.PI / 2, -Math.PI / 2);
        }

        if (getCubeType(pos.add(new Vec3(0, -1, 0))) == null) {
            Graphics3D.drawQuadFast(pos.add(new Vec3(delta)), new Vec2(1 - 2 * delta), Math.PI / 2, 0);
        }
        if (getCubeType(pos.add(new Vec3(0, 1, 0))) == null) {
            Graphics3D.drawQuadFast(pos.add(new Vec3(1 - delta)), new Vec2(1 - 2 * delta), -Math.PI / 2, Math.PI);
        }
    }
}
