package invisibleman;

import engine.AbstractEntity;
import engine.Core;
import graphics.Graphics3D;
import graphics.Window3D;
import graphics.data.Sprite;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import static org.lwjgl.opengl.GL11.*;
import static util.Color4.WHITE;
import util.Vec2;
import util.Vec3;

public class Snow extends AbstractEntity {

    private static final int MAX_DIST = 10, MIN_DIST = 1;

    @Override
    public void create() {
        List<Particle> particles = new LinkedList();
        for (int i = 0; i < 1000; i++) {
            particles.add(new Particle(Vec2.randomCircle(MAX_DIST).toVec3().withZ(Math.random() * 5), Math.random() * .04 + .06));
        }
        onUpdate(dt -> {
            Iterator<Particle> it = particles.iterator();
            while (it.hasNext()) {
                Particle p = it.next();
                p.pos = p.pos.add(Vec3.randomSquare(dt / 2).add(new Vec3(0, 0, -dt)));
                if (p.pos.z < 0) {
                    p.pos = p.pos.withZ(p.pos.z + 5);
                }
                if (p.pos.toVec2().lengthSquared() < MIN_DIST * MIN_DIST) {
                    p.pos = p.pos.toVec2().withLength(MIN_DIST).toVec3().withZ(p.pos.z);
                }
                if (p.pos.toVec2().lengthSquared() > MAX_DIST * MAX_DIST) {
                    p.pos = p.pos.toVec2().withLength(MAX_DIST).toVec3().withZ(p.pos.z);
                }
            }
        });

        Sprite s = new Sprite("snowflake2");
        Core.renderLayer(2).onEvent(() -> {
            glEnable(GL_TEXTURE_2D);
            s.getTexture().bind();
            WHITE.glColor();
            glBegin(GL_QUADS);
            particles.forEach(p -> {
                Vec3 pos = p.pos.add(Window3D.pos.withZ(0));
                Vec3 towards = pos.subtract(Window3D.pos);
                Vec3 side = towards.cross(Window3D.UP).withLength(p.size / 2);
                Vec3 snowUp = towards.cross(side).withLength(p.size / 2);
                Graphics3D.drawSpriteFast(s.getTexture(), pos.add(side).add(snowUp), pos.subtract(side).add(snowUp),
                        pos.subtract(side).subtract(snowUp), pos.add(side).subtract(snowUp), towards.reverse());
//                s.draw(pos.subtract(p.pos.cross(Window3D.UP).withLength(-s.scale.x / 2)),
//                        -p.pos.direction2() + Math.PI / 2, p.pos.direction() + Math.PI / 2);
            });
            glEnd();
        }).addChild(this);
    }

    private static class Particle {

        private Vec3 pos;
        private double size;

        public Particle(Vec3 pos, double size) {
            this.pos = pos;
            this.size = size;
        }
    }
}
