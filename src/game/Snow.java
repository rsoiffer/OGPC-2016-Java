package game;

import engine.Core;
import graphics.Graphics3D;
import graphics.Window3D;
import graphics.data.Sprite;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import map.CubeMap;
import static org.lwjgl.opengl.GL11.*;
import static util.Color4.WHITE;
import util.RegisteredEntity;
import util.Vec3;

public class Snow extends RegisteredEntity {

    private static final int MAX_DIST = 15;

    @Override
    public void createInner() {
        List<Particle> particles = new LinkedList();
        for (int i = 0; i < 2500; i++) {
            particles.add(new Particle(Window3D.pos.add(Vec3.randomSquare(MAX_DIST)), Math.random() * .04 + .06));
        }
        onUpdate(dt -> {
            Iterator<Particle> it = particles.iterator();
            while (it.hasNext()) {
                Particle p = it.next();
                p.pos = p.pos.add(Vec3.randomSquare(dt / 5).add(new Vec3(0, 0, -dt)));
                p.pos = Window3D.pos.add(p.pos.subtract(Window3D.pos).perComponent(d -> (d + 3 * MAX_DIST) % (2 * MAX_DIST) - MAX_DIST));
            }
        });

        Sprite s = new Sprite("snowflake2");
        Core.renderLayer(.2).onEvent(() -> {
            glEnable(GL_TEXTURE_2D);
            s.getTexture().bind();
            WHITE.glColor();
            glBegin(GL_QUADS);
            particles.forEach(p -> {
                if (CubeMap.rayCastStream(p.pos, new Vec3(0, 0, 1)).anyMatch(cd -> cd.c != null)) {
                    return;
                };
                Vec3 towards = p.pos.subtract(Window3D.pos);
                Vec3 side = towards.cross(Window3D.UP).withLength(p.size / 2);
                Vec3 snowUp = towards.cross(side).withLength(p.size / 2);
                Graphics3D.drawQuadFastT(p.pos.add(side).add(snowUp), p.pos.subtract(side).add(snowUp),
                        p.pos.subtract(side).subtract(snowUp), p.pos.add(side).subtract(snowUp));
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
