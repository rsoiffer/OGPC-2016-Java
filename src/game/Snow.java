package game;

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
import util.Noise;
import util.Vec2;
import util.Vec3;

public class Snow extends AbstractEntity {

    private static final int MAX_DIST = 10, MIN_DIST = 1;

    @Override
    public void create() {
        List<Particle> particles = new LinkedList();
        for (int i = 0; i < 1500; i++) {
            particles.add(new Particle(Vec2.randomCircle(MAX_DIST).toVec3().withZ(Math.random() * 5), Math.random() * .04 + .06));
        }
        onUpdate(dt -> {
            Iterator<Particle> it = particles.iterator();
            while (it.hasNext()) {
                Particle p = it.next();
                p.pos = p.pos.add(Vec3.randomSquare(dt / 10).add(new Vec3(0, 0, -dt * (p.getDR() + .5) / 2)));
                if (p.pos.z < 0) {
                    p.pos = p.pos.withZ(p.pos.z + 5);
                }
                if (p.pos.toVec2().lengthSquared() < MIN_DIST * MIN_DIST) {
                    p.pos = p.pos.toVec2().withLength(MIN_DIST).toVec3().withZ(p.pos.z);
                }
                if (p.pos.toVec2().lengthSquared() > MAX_DIST * MAX_DIST) {
                    p.pos = p.pos.toVec2().withLength(MAX_DIST).toVec3().withZ(p.pos.z);
                }
                p.pos = p.pos.add(Snow.Particle.getWind(dt / 10).toVec3());
                p.setRotation((p.getRotation() + dt * p.getDR()) % Math.PI);
            }
        });

        Sprite s = new Sprite("snowflake2");
        Core.renderLayer(2).onEvent(() -> {
            glEnable(GL_TEXTURE_2D);
            s.getTexture().bind();
            WHITE.glColor();
            glBegin(GL_QUADS);
            particles.forEach(p -> {
                Vec3 pos = p.pos.add(Window3D.pos.subtract(new Vec3(0, 0, 1)));
                Vec3 towards = pos.subtract(Window3D.pos);
                Vec3 side = towards.cross(Window3D.UP).withLength(Math.cos(p.getRotation()) * p.size / 2);
                Vec3 snowUp = towards.cross(side).withLength(p.size / 2);
                Graphics3D.drawSpriteFast(s.getTexture(), pos.add(side).add(snowUp), pos.subtract(side).add(snowUp),
                        pos.subtract(side).subtract(snowUp), pos.add(side).subtract(snowUp), towards.reverse());
//                s.draw(pos.subtract(p.pos.cross(Window3D.UP).withLength(-s.scale.x / 2)),
//                        -p.pos.direction2() + Math.PI / 2, p.pos.direction() + Math.PI / 2);
            });
            /*particles.forEach(p -> {
                Vec3 pos = p.pos.add(Window3D.pos.withZ(0));
                Vec3 towards = pos.subtract(Window3D.pos);
                Vec3 side = towards.cross(Window3D.UP).withLength(Math.cos(p.getRotation()+Math.PI/2)*p.size / 2);
                Vec3 snowUp = towards.cross(side).withLength(p.size / 2);
                Graphics3D.drawSpriteFast(s.getTexture(), pos.add(side).add(snowUp), pos.subtract(side).add(snowUp),
                        pos.subtract(side).subtract(snowUp), pos.add(side).subtract(snowUp), towards.reverse());
//                s.draw(pos.subtract(p.pos.cross(Window3D.UP).withLength(-s.scale.x / 2)),
//                        -p.pos.direction2() + Math.PI / 2, p.pos.direction() + Math.PI / 2);
            });*/

            glEnd();
        }).addChild(this);
    }

    private static class Particle {

        private static Vec2 wind;
        private static double windcount = 0;
        private static Noise x = new Noise(Math.random() * 400), y = new Noise(Math.random() * 400);

        private Vec3 pos;
        private double size;
        private double rotation = .25 + .75 * (Math.random() * Math.PI);
        private double dr = Math.random() * 2;

        public Particle(Vec3 pos, double size) {
            this.pos = pos;
            this.size = size;
        }

        public double getDR() {
            return dr;
        }

        public double getRotation() {
            return rotation;
        }

        public void setRotation(double r) {
            rotation = r;
        }

        public static Vec2 getWind(double dt) {
            windcount += 0.01;
            int m = 10;
            wind = new Vec2(m * dt * x.perlin(windcount, -windcount), m * dt * y.perlin(-windcount, windcount));
            //return wind;
            return Vec2.ZERO;
        }
    }
}
