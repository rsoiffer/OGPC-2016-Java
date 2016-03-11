package invisibleman;

import engine.Core;
import engine.Signal;
import graphics.Graphics3D;
import graphics.data.Sprite;
import graphics.data.Texture;
import graphics.loading.SpriteContainer;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import util.Color4;
import util.RegisteredEntity;
import util.Vec2;
import util.Vec3;

public class Footstep extends RegisteredEntity {

    @Override
    protected void createInner() {
        //Create the footstep's variables
        Signal<Vec3> position = Premade3D.makePosition(this);
        Signal<Double> rotation = Premade3D.makeRotation(this);
        onRender(() -> Fog.setMinTexColor(1, 1, 1, 0));
        Signal<Sprite> s = new Signal(new Sprite("footstep_white"));//Premade3D.makeSpriteGraphics(this, "footstep_white");
        onRender(() -> drawFootstep(position.get(), rotation.get(), s.get().scale.y > 0, s.get().color));
        onRender(() -> Fog.setMinTexColor(0, 0, 0, 0));

        //Make the footstep sink slightly so it does depth order correctly
        onUpdate(dt -> position.edit(new Vec3(0, 0, -dt / 10000)::add));

        //Opaqueness is low when the footstep is mostly transparent
        Signal<Double> opaqueness = Core.time().map(t -> .8 * Math.pow(.97, t));
        opaqueness.doForEach(d -> s.get().color = new Color4(1.0207 - d, 1.0207 - d, 1.0207 - d));

        //Destroy the footstep after 120 seconds (will probably change later)
        Core.timer(120, this::destroy);
    }

    public static void drawFootstep(Vec3 pos, double rot, boolean isLeft, Color4 color) {
        Vec2 nor = Tile.normalAt(pos);
        if (nor == null) {
            return;
        }

        Vec2 p0 = new Vec2(-.1, -.1 * (isLeft ? 1 : -1)).rotate(rot);
        Vec2 p1 = new Vec2(.1, -.1 * (isLeft ? 1 : -1)).rotate(rot);
        Vec2 p2 = new Vec2(.1, .1 * (isLeft ? 1 : -1)).rotate(rot);
        Vec2 p3 = new Vec2(-.1, .1 * (isLeft ? 1 : -1)).rotate(rot);

        Texture s = SpriteContainer.loadSprite("footstep_white");
        glEnable(GL_TEXTURE_2D);
        s.bind();
        glBegin(GL_QUADS);
        Graphics3D.drawSpriteFast(s, pos.add(p0.toVec3().withZ(p0.dot(nor))),
                pos.add(p1.toVec3().withZ(p1.dot(nor))),
                pos.add(p2.toVec3().withZ(p2.dot(nor))),
                pos.add(p3.toVec3().withZ(p3.dot(nor))), nor.toVec3().withZ(1));
        glEnd();
//        glPushMatrix();
//        glEnable(GL_TEXTURE_2D);
//        s.bind();
//        double dir = Math.signum(size.x * size.y);
//
//        color.glColor();
//        glTranslated(pos.x, pos.y, pos.z);
//        glRotated(angle * 180 / Math.PI, 0, 0, 1);
//        glRotated(tilt * 180 / Math.PI, 1, 0, 0);
//
//        glBegin(GL_QUADS);
//        {
//            glNormal3d(0, 0, dir);
//            glTexCoord2d(0, s.getHeight());
//            glVertex3d(0, 0, 0);
//            glNormal3d(0, 0, dir);
//            glTexCoord2d(s.getWidth(), s.getHeight());
//            glVertex3d(size.x, 0, 0);
//            glNormal3d(0, 0, dir);
//            glTexCoord2d(s.getWidth(), 0);
//            glVertex3d(size.x, size.y, 0);
//            glNormal3d(0, 0, dir);
//            glTexCoord2d(0, 0);
//            glVertex3d(0, size.y, 0);
//        }
//        glEnd();
//        glPopMatrix();
    }

    //Set the footstep's variables
    public void set(Vec3 pos, double rot, boolean isLeft) {
        get("position", Vec3.class).set(pos);
        get("rotation", Double.class).set(rot);
        get("sprite", Sprite.class).get().scale = isLeft ? new Vec2(.2, .2) : new Vec2(.2, -.2);
    }
}
