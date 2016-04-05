package mc_leveleditor;

import static mc_leveleditor.CubeMap.map;
import static mc_leveleditor.CubeType.DRAW_EDGES;
import static org.lwjgl.opengl.GL11.*;
import static util.Color4.BLACK;
import static util.Color4.WHITE;
import util.Util;
import util.Vec3;

public class Chunk {

    public static final int SIZE = 20;
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

    public void redraw() {
        glNewList(drawList, GL_COMPILE);
        WHITE.glColor();
        glEnable(GL_TEXTURE_2D);
        for (CubeType ct : CubeType.values()) {
            ct.texture.bind();
            glBegin(GL_QUADS);
            Util.forRange(x, x + SIZE, y, y + SIZE, (x, y) -> Util.forRange(z, z + SIZE, z -> {
                if (map[x][y][z] == ct) {
                    map[x][y][z].drawFaces(new Vec3(x, y, z));
                }
            }));
            glEnd();
        }
        if (DRAW_EDGES) {
            glLineWidth(2);
            BLACK.glColor();
            glDisable(GL_TEXTURE_2D);
            glBegin(GL_LINES);
            Util.forRange(x, x + SIZE, y, y + SIZE, (x, y) -> Util.forRange(z, z + SIZE, z -> {
                if (map[x][y][z] != null) {
                    map[x][y][z].drawEdges(new Vec3(x, y, z));
                }
            }));
            glEnd();
        }
        glEndList();
    }
}
