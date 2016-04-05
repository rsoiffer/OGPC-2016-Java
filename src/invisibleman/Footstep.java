package invisibleman;

import engine.Core;
import engine.Signal;
import graphics.Graphics3D;
import graphics.data.Sprite;
import graphics.data.Texture;
import graphics.loading.SpriteContainer;
import static org.lwjgl.opengl.GL11.*;
import util.Color4;
import util.RegisteredEntity;
import util.Vec2;
import util.Vec3;

public class Footstep extends RegisteredEntity {

    static {
        Core.render.onEvent(() -> {
            Fog.setMinTexColor(1, 1, 1, 0);
            Texture s = SpriteContainer.loadSprite("footstep_white");
            glEnable(GL_TEXTURE_2D);
            s.bind();
            glBegin(GL_QUADS);

            RegisteredEntity.getAll(Footstep.class).forEach(f -> drawFootstep(f.get("position", Vec3.class).get(),
                    f.get("rotation", Double.class).get(), f.get("sprite", Sprite.class).get().scale.y > 0, f.get("sprite", Sprite.class).get().color));

            glEnd();
            Fog.setMinTexColor(0, 0, 0, 0);
        });
    }

    @Override
    protected void createInner() {
        //Create the footstep's variables
        Signal<Vec3> position = Premade3D.makePosition(this);
        Signal<Double> rotation = Premade3D.makeRotation(this);
//        onRender(() -> Fog.setMinTexColor(1, 1, 1, 0));
        Signal<Sprite> s = addChild(new Signal(new Sprite("footstep_white")), "sprite");
//        onRender(() -> drawFootstep(position.get(), rotation.get(), s.get().scale.y > 0, s.get().color));
//        onRender(() -> Fog.setMinTexColor(0, 0, 0, 0));

        //Make the footstep sink slightly so it does depth order correctly
        onUpdate(dt -> position.edit(new Vec3(0, 0, -dt / 10000)::add));

        //Opaqueness is low when the footstep is mostly transparent
        Signal<Double> opaqueness = Core.time().map(t -> .8 * Math.pow(.97, t));
        opaqueness.doForEach(d -> s.get().color = new Color4(1.0207 - d, 1.0207 - d, 1.0207 - d));

        //Destroy the footstep after 120 seconds (will probably change later)
        Core.timer(120, this::destroy);
    }

    public static void drawFootstep(Vec3 pos, double rot, boolean isLeft, Color4 color) {
        color.glColor();
        Graphics3D.drawQuadFast(pos, new Vec2(.3, isLeft ? .3 : -.3), 0, rot);
//        Vec2 nor = new Vec2(0);//Tile.normalAt(pos);
//        if (nor == null) {
//            return;
//        }
//
//        Vec2 side = new Vec2(0, .1 * (isLeft ? 1 : -1)).rotate(rot);
//        pos = pos.add(side.toVec3().withZ(side.dot(nor)));
//
//        double mult = 1 / Math.sqrt(1 + nor.lengthSquared());
//        Vec2 p0 = new Vec2(-.1, -.1 * (isLeft ? 1 : -1)).rotate(rot).multiply(mult);
//        Vec2 p1 = new Vec2(.1, -.1 * (isLeft ? 1 : -1)).rotate(rot).multiply(mult);
//        Vec2 p2 = new Vec2(.1, .1 * (isLeft ? 1 : -1)).rotate(rot).multiply(mult);
//        Vec2 p3 = new Vec2(-.1, .1 * (isLeft ? 1 : -1)).rotate(rot).multiply(mult);
//
//        Texture s = SpriteContainer.loadSprite("footstep_white");
//        glEnable(GL_TEXTURE_2D);
//        s.bind();
//        color.glColor();
//        glBegin(GL_QUADS);
//        Graphics3D.drawSpriteFast(s, pos.add(p0.toVec3().withZ(p0.dot(nor))),
//                pos.add(p1.toVec3().withZ(p1.dot(nor))),
//                pos.add(p2.toVec3().withZ(p2.dot(nor))),
//                pos.add(p3.toVec3().withZ(p3.dot(nor))), nor.toVec3().withZ(1));
//        glEnd();
    }

    //Set the footstep's variables
    public void set(Vec3 pos, double rot, boolean isLeft) {
        get("position", Vec3.class).set(pos);
        get("rotation", Double.class).set(rot);
        get("sprite", Sprite.class).get().scale = isLeft ? new Vec2(.2, .2) : new Vec2(.2, -.2);
    }
}
