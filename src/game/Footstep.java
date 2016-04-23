package game;

import engine.Core;
import engine.Signal;
import graphics.Graphics3D;
import graphics.Window3D;
import graphics.data.Texture;
import graphics.loading.SpriteContainer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import util.Color4;
import static util.Color4.BLACK;
import util.RegisteredEntity;
import util.Sounds;
import util.Vec2;
import util.Vec3;

public class Footstep extends RegisteredEntity {

    private static List<Footstep> ALL_FOOTSTEPS = new ArrayList();
    private static String texFile;
    private static int footstepCount;

    static {

        texFile = "footstep_white";

        Core.renderLayer(.5).onEvent(() -> {
            Collections.sort(ALL_FOOTSTEPS, Comparator.comparingDouble(f -> -Math.abs(f.get("position", Vec3.class).get().z - Window3D.pos.z)));

            Texture footprint = SpriteContainer.loadSprite("footsteps/" + texFile);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glEnable(GL_TEXTURE_2D);
            footprint.bind();
            glBegin(GL_QUADS);

            ALL_FOOTSTEPS.forEach(f -> drawFootstep(f.get("position", Vec3.class).get(), f.get("rotation", Double.class).get(),
                    f.get("isLeft", Boolean.class).get(), BLACK.withA(f.get("opacity", Double.class).get())));

            glEnd();
        });
    }

    public static void changePrint(String fn) {

        texFile = fn;
    }

    @Override
    public void createInner() {
        ALL_FOOTSTEPS.add(this);
        //Create the footstep's variables
        Signal<Vec3> position = Premade3D.makePosition(this);
        Signal<Double> rotation = Premade3D.makeRotation(this);
        Signal<Boolean> isLeft = addChild(new Signal(false), "isLeft");
        Signal<Double> opacity = addChild(Core.update.reduce(1., (dt, x) -> x * Math.pow(.85, dt)), "opacity");

        //Make the footstep sink slightly so it does depth order correctly
        onUpdate(dt -> position.edit(new Vec3(0, 0, -dt / 10000)::add));

        //Destroy the footstep once it fades enough
        opacity.filter(x -> x < .0001).onEvent(this::destroy);

        footstepCount++;

        if (footstepCount % 3 == 0) {
            Sounds.playSound("Step" + ((int) Math.random() * 8 + 1) + ".mp3");
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        ALL_FOOTSTEPS.remove(this);
    }

    public static void drawFootstep(Vec3 pos, double rot, boolean isLeft, Color4 color) {
        color.glColor();
        Graphics3D.drawQuadFast(pos.subtract(new Vec2(.15, 0).rotate(rot).toVec3()), new Vec2(.3, isLeft ? .3 : -.3), 0, rot);
    }

    //Set the footstep's variables
    public void set(Vec3 pos, double rot, boolean isLeft, double opacity) {
        get("position", Vec3.class).set(pos);
        get("rotation", Double.class).set(rot);
        get("isLeft", Boolean.class).set(isLeft);
        get("opacity", Double.class).set(opacity);
    }
}
