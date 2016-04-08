package map;

import static map.CubeMap.map;
import static map.CubeType.DRAW_EDGES;
import static org.lwjgl.opengl.GL11.*;
import static util.Color4.BLACK;
import static util.Color4.WHITE;
import util.Util;
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

    public void redraw() {
        glNewList(drawList, GL_COMPILE);
        WHITE.glColor();
        glEnable(GL_TEXTURE_2D);
        for (CubeType ct : CubeType.values()) {
            ct.texture.bind();
            glBegin(GL_QUADS);
            Util.forRange(x, x + CHUNK_SIZE, y, y + CHUNK_SIZE, (x, y) -> Util.forRange(z, z + CHUNK_SIZE, z -> {
                if (map[x][y][z] == ct) {
                    CubeType.drawFaces(new Vec3(x, y, z));
                }
            }));
            glEnd();
        }
        if (DRAW_EDGES) {
            glLineWidth(1);
            BLACK.glColor();
            glDisable(GL_TEXTURE_2D);
            glBegin(GL_LINES);
            Util.forRange(x, x + CHUNK_SIZE, y, y + CHUNK_SIZE, (x, y) -> Util.forRange(z, z + CHUNK_SIZE, z -> {
                // if (map[x][y][z] != null) {
                CubeType.drawEdges(new Vec3(x, y, z));
                //}
            }));
            glEnd();
        }
        glEndList();
    }
}
