package invisibleman;

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
import util.Color4;
import static util.Color4.BLACK;
import util.RegisteredEntity;
import util.Vec2;
import util.Vec3;

public class Footstep extends RegisteredEntity {

    private static List<Footstep> all = new ArrayList();

    static {
        Core.renderLayer(.5).onEvent(() -> {
            Collections.sort(all, Comparator.comparingDouble(f -> -f.get("position", Vec3.class).get().subtract(Window3D.pos).lengthSquared()));

            Fog.setMinTexColor(1, 1, 1, 0);
            Texture s = SpriteContainer.loadSprite("footstep_white");
            glEnable(GL_TEXTURE_2D);
            s.bind();
            glBegin(GL_QUADS);

            all.forEach(f -> drawFootstep(f.get("position", Vec3.class).get(), f.get("rotation", Double.class).get(),
                    f.get("isLeft", Boolean.class).get(), BLACK.withA(f.get("opacity", Double.class).get())));

            glEnd();
            Fog.setMinTexColor(0, 0, 0, 0);
        });
    }

    @Override
    public void createInner() {
        all.add(this);
        //Create the footstep's variables
        Signal<Vec3> position = Premade3D.makePosition(this);
        Signal<Double> rotation = Premade3D.makeRotation(this);
        Signal<Boolean> isLeft = addChild(new Signal(false), "isLeft");
        Signal<Double> opacity = addChild(Core.time().map(t -> Math.pow(.99, t)), "opacity");

        //Make the footstep sink slightly so it does depth order correctly
        onUpdate(dt -> position.edit(new Vec3(0, 0, -dt / 10000)::add));

        //Destroy the footstep after 5 minutes (will probably change later)
        Core.timer(300, this::destroy);
    }

    @Override
    public void destroy() {
        super.destroy();
        all.remove(this);
    }

    public static void drawFootstep(Vec3 pos, double rot, boolean isLeft, Color4 color) {
        color.glColor();
        Graphics3D.drawQuadFast(pos, new Vec2(.3, isLeft ? .3 : -.3), 0, rot);
    }

    //Set the footstep's variables
    public void set(Vec3 pos, double rot, boolean isLeft) {
        get("position", Vec3.class).set(pos);
        get("rotation", Double.class).set(rot);
        get("isLeft", Boolean.class).set(isLeft);
    }
}
