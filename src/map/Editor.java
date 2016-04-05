package map;

import com.codepoetics.protonpack.StreamUtils;
import engine.AbstractEntity.LAE;
import engine.Core;
import engine.Input;
import graphics.Camera;
import graphics.Graphics2D;
import graphics.Window3D;
import static graphics.Window3D.*;
import invisibleman.Fog;
import invisibleman.Premade3D;
import java.io.PrintWriter;
import java.util.function.Supplier;
import static map.CubeMap.*;
import static map.CubeType.SNOW;
import static org.lwjgl.input.Keyboard.*;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import static util.Color4.*;
import util.*;

public class Editor {

    public static void main(String[] args) {
        //Initial graphics setup
        Core.is3D = true;
        Core.init();
        Core.render.bufferCount(Core.interval(1)).forEach(i -> Display.setTitle("FPS: " + i));
        new Fog(Color4.gray(.8), .00025, 1).create();
        Mouse.setGrabbed(true);

        //Selecting blocks
        Mutable<Integer> selected = new Mutable(0);
        Input.mouseWheel.forEach(x -> selected.o = (selected.o + x / 120 + CubeType.values().length) % CubeType.values().length);

        //All drawing code
        Core.render.onEvent(() -> {
            CubeMap.drawAll();

            //Draw GUI
            Camera.setProjection2D(new Vec2(0), new Vec2(1200, 800));

            Fog.setMinTexColor(1, 1, 1, 1);
            Graphics2D.drawEllipse(new Vec2(600, 400), new Vec2(10), RED, 20);
            Fog.setMinTexColor(0, 0, 0, 0);

            Graphics2D.drawText("Hello", new Vec2(100));

            Fog.setMinTexColor(1, 1, 1, 1);
            Graphics2D.fillRect(new Vec2(545, 145), new Vec2(110), gray(.5));
            Graphics2D.drawRect(new Vec2(545, 145), new Vec2(110), BLACK);
            Fog.setMinTexColor(0, 0, 0, 0);

            Graphics2D.drawSprite(CubeType.values()[selected.o].texture, new Vec2(600, 200), new Vec2(100. / CubeType.values()[selected.o].texture.getImageWidth()), 0, WHITE);

            Fog.setMinTexColor(1, 1, 1, 1);
            Graphics2D.drawRect(new Vec2(550, 150), new Vec2(100), BLACK);
            Fog.setMinTexColor(0, 0, 0, 0);

            Window3D.resetProjection();
        });

        //Movement
        Premade3D.makeMouseLook(new LAE(e -> {
        }), 2, -1.5, 1.5);
        Supplier<Double> speed = Input.keySignal(KEY_TAB).map(b -> b ? 20. : 5);
        Input.whileKeyDown(KEY_W).forEach(dt -> pos = pos.add(facing.toVec3().multiply(speed.get() * dt)));
        Input.whileKeyDown(KEY_S).forEach(dt -> pos = pos.add(facing.toVec3().multiply(-speed.get() * dt)));
        Input.whileKeyDown(KEY_A).forEach(dt -> pos = pos.add(facing.toVec3().cross(UP).withLength(-speed.get() * dt)));
        Input.whileKeyDown(KEY_D).forEach(dt -> pos = pos.add(facing.toVec3().cross(UP).withLength(speed.get() * dt)));
        Input.whileKeyDown(KEY_SPACE).forEach(dt -> pos = pos.add(UP.multiply(speed.get() * dt)));
        Input.whileKeyDown(KEY_LSHIFT).forEach(dt -> pos = pos.add(UP.multiply(-speed.get() * dt)));

        //Collisions
//        Mutable<Vec3> oldPos = new Mutable(pos);
//        Core.update.forEach(dt -> {
//            if (CubeMap.getCubeType(pos) != null) {
//                pos = oldPos.o;
//            } else {
//                oldPos.o = pos;
//            }
//        });
        //Initial level
        Util.forRange(0, WIDTH, 0, DEPTH, (x, y) -> Util.forRange(0, 10, z -> {
            map[x][y][z] = SNOW;
        }));
        CubeMap.redrawAll();
        pos = new Vec3(10, 10, 15);

        //Destroy blocks
        Input.whenMouse(0, true).onEvent(() -> {
            CubeMap.rayCastStream(pos, facing.toVec3()).filter(cd -> cd.c != null).findFirst().ifPresent(cd -> {
                CubeMap.map[cd.x][cd.y][cd.z] = null;
                CubeMap.redraw(new Vec3(cd.x, cd.y, cd.z));
            });
        });

        //Place blocks
        Input.whenMouse(1, true).onEvent(() -> {
            StreamUtils.takeWhile(CubeMap.rayCastStream(pos, facing.toVec3()).skip(1), cd -> cd.c == null).reduce((a, b) -> b).ifPresent(cd -> {
                CubeMap.map[cd.x][cd.y][cd.z] = CubeType.values()[selected.o];
                CubeMap.redraw(new Vec3(cd.x, cd.y, cd.z));
            });
        });

        //Repaint blocks
        Input.whileMouse(2, true).onEvent(() -> {
            CubeMap.rayCastStream(pos, facing.toVec3()).filter(cd -> cd.c != null).findFirst().ifPresent(cd -> {
                CubeMap.map[cd.x][cd.y][cd.z] = CubeType.values()[selected.o];
                CubeMap.redraw(new Vec3(cd.x, cd.y, cd.z));
            });
        });

        //Save and load
        Input.whenKey(KEY_RETURN, true).onEvent(() -> {
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
