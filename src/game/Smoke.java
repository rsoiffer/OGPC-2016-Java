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
import util.Color4;
import static util.Color4.WHITE;
import util.RegisteredEntity;
import util.Vec2;
import util.Vec3;

public class Smoke extends RegisteredEntity {

    private static List<Smoke> ALL_SMOKE = new ArrayList();

    static {
        Core.renderLayer(.6).onEvent(() -> {
            Collections.sort(ALL_SMOKE, Comparator.comparingDouble(f -> -f.get("position", Vec3.class).get().subtract(Window3D.pos).dot(Window3D.facing.toVec3())));

            Texture s = SpriteContainer.loadSprite("smoke");
            glEnable(GL_TEXTURE_2D);
            s.bind();
            glBegin(GL_QUADS);

            ALL_SMOKE.forEach(f -> drawSmoke(f.get("position", Vec3.class).get(), WHITE.withA(f.get("opacity", Double.class).get())));

            glEnd();
        });
    }

    @Override
    public void createInner() {
        ALL_SMOKE.add(this);
        Signal<Vec3> position = Premade3D.makePosition(this);
        Signal<Double> opacity = addChild(Core.update.reduce(1., (dt, x) -> x * Math.pow(.97, dt)), "opacity");
        opacity.filter(x -> x < .0001).onEvent(this::destroy);
    }

    @Override
    public void destroy() {
        super.destroy();
        ALL_SMOKE.remove(this);
    }

    public static void drawSmoke(Vec3 pos, Color4 color) {
        Vec3 towardsSprite = Window3D.facing.toVec3().withLength(pos.subtract(Window3D.pos).dot(Window3D.facing.toVec3()));
        color.withA(color.a * towardsSprite.length() / (4 + towardsSprite.lengthSquared() / 10)).glColor();
        Graphics3D.drawQuadFast(pos.subtract(towardsSprite.cross(Window3D.UP).withLength(-.25)),
                new Vec2(.5), Math.PI / 2 - towardsSprite.direction2(), towardsSprite.direction() + Math.PI / 2);
    }
}
