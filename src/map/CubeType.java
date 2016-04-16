package map;

import graphics.Graphics3D;
import graphics.data.Texture;
import graphics.loading.SpriteContainer;
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
    WOOD8_B("wood_b8");

    public final Texture texture;

    private CubeType(String name) {
        texture = SpriteContainer.loadSprite(name);
    }

    public static boolean DRAW_EDGES = true;

    public static void drawEdges(Vec3 pos) {
        double d = 0.05;
        int[][][] isSolid = new int[2][2][2];
        Util.forRange(0, 2, 0, 2, (x, y) -> Util.forRange(0, 2, z -> {
            isSolid[x][y][z] = CubeMap.isSolid(pos.add(new Vec3(x, y, z)))? 0 : 1;
        }));
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 2; k++) {
                    if(isSolid[i][j][k] == 1) continue;
                    Vec3 dir = new Vec3(i==0?-1:1,j==0?-1:1,k==0?-1:1);
//                    
//                    if((isSolid[i][1-j][k] | isSolid[i][j][1-k])==0){
//                        pos.add(new Vec3(i,j,k+10)).multiply(dir.withX(0)).multiply(d).glVertex();
//                        pos.add(new Vec3(i,j,k+10)).multiply(dir.withX(0)).multiply(d).add(Vec3.ZERO.withX(dir.x*(1-(CubeMap.isSolid(pos.add(new Vec3(i+dir.x,j,k)))?1:0)))).glVertex();
//                    }
//                    if((isSolid[1-i][j][k] | isSolid[i][j][1-k])==0){
//                        pos.add(new Vec3(i,j,k+10)).multiply(dir.withY(0)).multiply(d).glVertex();
//                        pos.add(new Vec3(i,j,k+10)).multiply(dir.withY(0)).multiply(d).add(Vec3.ZERO.withY(dir.y*(1-(CubeMap.isSolid(pos.add(new Vec3(i,j+dir.y,k)))?1:0)))).glVertex();
//                    }
//                    if((isSolid[1-i][j][k] | isSolid[i][1-j][k])==0){
//                        pos.add(new Vec3(i,j,k+10)).multiply(dir.withZ(0)).multiply(d).glVertex();
//                        pos.add(new Vec3(i,j,k+10)).multiply(dir.withZ(0)).multiply(d).add(Vec3.ZERO.withX(dir.x*(1-(CubeMap.isSolid(pos.add(new Vec3(i,j,k+dir.z)))?1:0)))).glVertex();
//                    }

                    if (isSolid[0][0][0] + isSolid[1][1][0] != isSolid[1][0][0] + isSolid[0][1][0]) {
                        pos.add(new Vec3(1, 1, 0)).glVertex();
                        pos.add(new Vec3(1)).glVertex();
                    }
                    if (isSolid[0][0][0] + isSolid[1][0][1] != isSolid[1][0][0] + isSolid[0][0][1]) {
                        pos.add(new Vec3(1, 0, 1)).glVertex();
                        pos.add(new Vec3(1)).glVertex();
                    }
                    if (isSolid[0][0][0] + isSolid[0][1][1] != isSolid[0][1][0] + isSolid[0][0][1]) {
                        pos.add(new Vec3(0, 1, 1)).glVertex();
                        pos.add(new Vec3(1)).glVertex();
                    }
                }
            }
        }
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
