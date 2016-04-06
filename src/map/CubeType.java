package map;

import graphics.Graphics3D;
import graphics.Window3D;
import graphics.data.Texture;
import graphics.loading.SpriteContainer;
import static map.CubeMap.getCubeType;
import util.Vec2;
import util.Vec3;

public enum CubeType {

    SNOW("white_pixel"),
    STONE("stone_floor"),
    GRASS("grass_floor"),
    ROCK("rockdiffuse"),
    ICE("ice1");

    public final Texture texture;

    private CubeType(String name) {
        texture = SpriteContainer.loadSprite(name);
    }

    public static final boolean DRAW_EDGES = true;

    public void drawEdges(Vec3 pos) {
        //if (Window3D.pos.z < pos.z) {
        if (getCubeType(pos.add(new Vec3(0, 0, -1))) == null) {
            Graphics3D.drawQuadFastLines(pos, new Vec2(1), 0, 0);
        }
        //} else if (Window3D.pos.z > pos.z + 1) {
        if (getCubeType(pos.add(new Vec3(0, 0, 1))) == null) {
            Graphics3D.drawQuadFastLines(pos.add(new Vec3(1)), new Vec2(1), 0, Math.PI);
        }
        //}

        //if (Window3D.pos.x < pos.x) {
        if (getCubeType(pos.add(new Vec3(-1, 0, 0))) == null) {
            Graphics3D.drawQuadFastLines(pos, new Vec2(1), Math.PI / 2, Math.PI / 2);
        }
        //} else if (Window3D.pos.x > pos.x + 1) {
        if (getCubeType(pos.add(new Vec3(1, 0, 0))) == null) {
            Graphics3D.drawQuadFastLines(pos.add(new Vec3(1)), new Vec2(1), -Math.PI / 2, -Math.PI / 2);
        }
        //}

        //if (Window3D.pos.y < pos.y) {
        if (getCubeType(pos.add(new Vec3(0, -1, 0))) == null) {
            Graphics3D.drawQuadFastLines(pos, new Vec2(1), Math.PI / 2, 0);
        }
        //} else if (Window3D.pos.y > pos.y + 1) {
        if (getCubeType(pos.add(new Vec3(0, 1, 0))) == null) {
            Graphics3D.drawQuadFastLines(pos.add(new Vec3(1)), new Vec2(1), -Math.PI / 2, Math.PI);
        }
        //}
    }

    public void drawFaces(Vec3 pos) {
        double delta = DRAW_EDGES ? Math.min(.005, pos.subtract(Window3D.pos).length() * .0005) : 0;
        //if (Window3D.pos.z < pos.z) {
        if (getCubeType(pos.add(new Vec3(0, 0, -1))) == null) {
            Graphics3D.drawQuadFast(pos.add(new Vec3(delta)), new Vec2(1 - 2 * delta), 0, 0);
        }
        //} else if (Window3D.pos.z > pos.z + 1) {
        if (getCubeType(pos.add(new Vec3(0, 0, 1))) == null) {
            //System.out.println("hi");
            Graphics3D.drawQuadFast(pos.add(new Vec3(1 - delta)), new Vec2(1 - 2 * delta), 0, Math.PI);
        }
        //}

        //if (Window3D.pos.x < pos.x) {
        if (getCubeType(pos.add(new Vec3(-1, 0, 0))) == null) {
            Graphics3D.drawQuadFast(pos.add(new Vec3(delta)), new Vec2(1 - 2 * delta), Math.PI / 2, Math.PI / 2);
        }
        //} else if (Window3D.pos.x > pos.x + 1) {
        if (getCubeType(pos.add(new Vec3(1, 0, 0))) == null) {
            Graphics3D.drawQuadFast(pos.add(new Vec3(1 - delta)), new Vec2(1 - 2 * delta), -Math.PI / 2, -Math.PI / 2);
        }
        //}

        //if (Window3D.pos.y < pos.y) {
        if (getCubeType(pos.add(new Vec3(0, -1, 0))) == null) {
            Graphics3D.drawQuadFast(pos.add(new Vec3(delta)), new Vec2(1 - 2 * delta), Math.PI / 2, 0);
        }
        //} else if (Window3D.pos.y > pos.y + 1) {
        if (getCubeType(pos.add(new Vec3(0, 1, 0))) == null) {
            Graphics3D.drawQuadFast(pos.add(new Vec3(1 - delta)), new Vec2(1 - 2 * delta), -Math.PI / 2, Math.PI);
        }
        //}
    }

//    public void draw(Vec3 pos) {
//        //double delta = .005;
//        double delta = pos.subtract(Window3D.pos).length() * .0005;
//        if (Window3D.pos.z < pos.z) {
//            if (getCubeType(pos.add(new Vec3(0, 0, -1))) == null) {
//                Graphics3D.drawQuadFast(pos.add(new Vec3(delta)), new Vec2(1 - 2 * delta), 0, 0, WHITE);
//                Graphics3D.drawQuadFastLines((pos, new Vec2(1), 0, 0, BLACK);
//            }
//        } else if (Window3D.pos.z > pos.z + 1) {
//            if (getCubeType(pos.add(new Vec3(0, 0, 1))) == null) {
//                Graphics3D.drawQuadFast(pos.add(new Vec3(1 - delta)), new Vec2(1 - 2 * delta), 0, Math.PI, WHITE);
//                Graphics3D.drawQuadFastLines((pos.add(new Vec3(1)), new Vec2(1), 0, Math.PI, BLACK);
//            }
//        }
//
//        if (Window3D.pos.x < pos.x) {
//            if (getCubeType(pos.add(new Vec3(-1, 0, 0))) == null) {
//                Graphics3D.drawQuadFast(pos.add(new Vec3(delta)), new Vec2(1 - 2 * delta), Math.PI / 2, Math.PI / 2, WHITE);
//                Graphics3D.drawQuadFastLines((pos, new Vec2(1), Math.PI / 2, Math.PI / 2, BLACK);
//            }
//        } else if (Window3D.pos.x > pos.x + 1) {
//            if (getCubeType(pos.add(new Vec3(1, 0, 0))) == null) {
//                Graphics3D.drawQuadFast(pos.add(new Vec3(1 - delta)), new Vec2(1 - 2 * delta), -Math.PI / 2, -Math.PI / 2, WHITE);
//                Graphics3D.drawQuadFastLines((pos.add(new Vec3(1)), new Vec2(1), -Math.PI / 2, -Math.PI / 2, BLACK);
//            }
//        }
//
//        if (Window3D.pos.y < pos.y) {
//            if (getCubeType(pos.add(new Vec3(0, -1, 0))) == null) {
//                Graphics3D.drawQuadFast(pos.add(new Vec3(delta)), new Vec2(1 - 2 * delta), Math.PI / 2, 0, WHITE);
//                Graphics3D.drawQuadFastLines((pos, new Vec2(1), Math.PI / 2, 0, BLACK);
//            }
//        } else if (Window3D.pos.y > pos.y + 1) {
//            if (getCubeType(pos.add(new Vec3(0, 1, 0))) == null) {
//                Graphics3D.drawQuadFast(pos.add(new Vec3(1 - delta)), new Vec2(1 - 2 * delta), -Math.PI / 2, Math.PI, WHITE);
//                Graphics3D.drawQuadFastLines((pos.add(new Vec3(1)), new Vec2(1), -Math.PI / 2, Math.PI, BLACK);
//            }
//        }
//
//        if (Window3D.pos.z < pos.z) {
//            if (getCubeType(pos.add(new Vec3(0, 0, -1))) == null) {
//                Graphics3D.drawQuadFast(pos, new Vec2(1), 0, 0, WHITE);
//                Graphics3D.drawQuadFastLines((pos.add(new Vec3(-delta)), new Vec2(1), 0, 0, BLACK);
//            }
//        } else if (Window3D.pos.z > pos.z + 1) {
//            if (getCubeType(pos.add(new Vec3(0, 0, 1))) == null) {
//                Graphics3D.drawQuadFast(pos.add(new Vec3(1)), new Vec2(1), 0, Math.PI, WHITE);
//                Graphics3D.drawQuadFastLines((pos.add(new Vec3(1 + delta)), new Vec2(1), 0, Math.PI, BLACK);
//            }
//        }
//
//        if (Window3D.pos.x < pos.x) {
//            if (getCubeType(pos.add(new Vec3(-1, 0, 0))) == null) {
//                Graphics3D.drawQuadFast(pos, new Vec2(1), Math.PI / 2, Math.PI / 2, WHITE);
//                Graphics3D.drawQuadFastLines((pos.add(new Vec3(-delta)), new Vec2(1), Math.PI / 2, Math.PI / 2, BLACK);
//            }
//        } else if (Window3D.pos.x > pos.x + 1) {
//            if (getCubeType(pos.add(new Vec3(1, 0, 0))) == null) {
//                Graphics3D.drawQuadFast(pos.add(new Vec3(1)), new Vec2(1), -Math.PI / 2, -Math.PI / 2, WHITE);
//                Graphics3D.drawQuadFastLines((pos.add(new Vec3(1 + delta)), new Vec2(1), -Math.PI / 2, -Math.PI / 2, BLACK);
//            }
//        }
//
//        if (Window3D.pos.y < pos.y) {
//            if (getCubeType(pos.add(new Vec3(0, -1, 0))) == null) {
//                Graphics3D.drawQuadFast(pos, new Vec2(1), Math.PI / 2, 0, WHITE);
//                Graphics3D.drawQuadFastLines((pos.add(new Vec3(-delta)), new Vec2(1), Math.PI / 2, 0, BLACK);
//            }
//        } else if (Window3D.pos.y > pos.y + 1) {
//            if (getCubeType(pos.add(new Vec3(0, 1, 0))) == null) {
//                Graphics3D.drawQuadFast(pos.add(new Vec3(1)), new Vec2(1), -Math.PI / 2, Math.PI, WHITE);
//                Graphics3D.drawQuadFastLines((pos.add(new Vec3(1 + delta)), new Vec2(1), -Math.PI / 2, Math.PI, BLACK);
//            }
//        }
}
