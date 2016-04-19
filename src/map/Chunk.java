package map;

import graphics.Graphics3D;
import static java.util.stream.Stream.of;
import static map.CubeMap.getCubeType;
import static org.lwjgl.opengl.GL11.*;
import static util.Color4.BLACK;
import static util.Color4.WHITE;
import util.Util;
import util.Vec2;
import util.Vec3;

public class Chunk {

    public static final int CHUNK_SIZE = 20;
    public final int x, y, z;
    public final int drawList;

    public Chunk(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        drawList = glGenLists(1);
    }

    public void draw() {
        glCallList(drawList);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Chunk other = (Chunk) obj;
        if (this.drawList != other.drawList) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + this.drawList;
        return hash;
    }

    public void redraw() {
        
        glNewList(drawList, GL_COMPILE);
        WHITE.glColor();
        glEnable(GL_TEXTURE_2D);
        for (CubeType ct : CubeType.getAll()) {
            ct.texture.bind();
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glBegin(GL_QUADS);
            Util.forRange(x, x + CHUNK_SIZE, y, y + CHUNK_SIZE, (x, y) -> Util.forRange(z, z + CHUNK_SIZE, z -> {
                if (getCubeType(x, y, z) == ct) {
                    drawFaces(new Vec3(x, y, z));
                }
            }));
            glEnd();
        }
        if (DRAW_EDGES) {
            glLineWidth(2);
            BLACK.glColor();
            glDisable(GL_TEXTURE_2D);
            glBegin(GL_LINES);
            Util.forRange(x, x + CHUNK_SIZE, y, y + CHUNK_SIZE, (x, y) -> Util.forRange(z, z + CHUNK_SIZE, z -> {
                // if (map[x][y][z] != null) {
                drawEdges(new Vec3(x, y, z));
                //}
            }));
            glEnd();
        }
        glEndList();
    }

    @Override
    public String toString() {
        return "Chunk{" + "x=" + x + ", y=" + y + ", z=" + z + ", drawList=" + drawList + '}';
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
        if (getCubeType(pos.add(new Vec3(0, 0, -1))) == null) {
            Graphics3D.drawQuadFast(pos, new Vec2(1), 0, 0);
        }
        if (getCubeType(pos.add(new Vec3(0, 0, 1))) == null) {
            Graphics3D.drawQuadFast(pos.add(new Vec3(0, 0, 1)), new Vec2(1), 0, 0);
        }

        if (getCubeType(pos.add(new Vec3(-1, 0, 0))) == null) {
            Graphics3D.drawQuadFast(pos, new Vec2(1), Math.PI / 2, Math.PI / 2);
        }
        if (getCubeType(pos.add(new Vec3(1, 0, 0))) == null) {
            Graphics3D.drawQuadFast(pos.add(new Vec3(1, 0, 0)), new Vec2(1), Math.PI / 2, Math.PI / 2);
        }

        if (getCubeType(pos.add(new Vec3(0, -1, 0))) == null) {
            Graphics3D.drawQuadFast(pos, new Vec2(1), Math.PI / 2, 0);
        }
        if (getCubeType(pos.add(new Vec3(0, 1, 0))) == null) {
            Graphics3D.drawQuadFast(pos.add(new Vec3(0, 1, 0)), new Vec2(1), Math.PI / 2, 0);
        }
//        double delta = 0;
//        if (getCubeType(pos.add(new Vec3(0, 0, -1))) == null) {
//            Graphics3D.drawQuadFast(pos.add(new Vec3(delta)), new Vec2(1 - 2 * delta), 0, 0);
//        }
//        if (getCubeType(pos.add(new Vec3(0, 0, 1))) == null) {
//            Graphics3D.drawQuadFast(pos.add(new Vec3(1 - delta)), new Vec2(1 - 2 * delta), 0, Math.PI);
//        }
//
//        if (getCubeType(pos.add(new Vec3(-1, 0, 0))) == null) {
//            Graphics3D.drawQuadFast(pos.add(new Vec3(delta)), new Vec2(1 - 2 * delta), Math.PI / 2, Math.PI / 2);
//        }
//        if (getCubeType(pos.add(new Vec3(1, 0, 0))) == null) {
//            Graphics3D.drawQuadFast(pos.add(new Vec3(1 - delta)), new Vec2(1 - 2 * delta), -Math.PI / 2, -Math.PI / 2);
//        }
//
//        if (getCubeType(pos.add(new Vec3(0, -1, 0))) == null) {
//            Graphics3D.drawQuadFast(pos.add(new Vec3(delta)), new Vec2(1 - 2 * delta), Math.PI / 2, 0);
//        }
//        if (getCubeType(pos.add(new Vec3(0, 1, 0))) == null) {
//            Graphics3D.drawQuadFast(pos.add(new Vec3(1 - delta)), new Vec2(1 - 2 * delta), -Math.PI / 2, Math.PI);
//        }
    }
}
