package invisibleman;

import engine.AbstractEntity;
import graphics.Window3D;
import graphics.data.Sprite;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import util.Vec2;
import util.Vec3;

public class Snow extends AbstractEntity {

    private static final int MAX_DIST = 10, MIN_DIST = 1;

    @Override
    public void create() {
        List<Particle> particles = new LinkedList();
        for (int i = 0; i < 1000; i++) {
            particles.add(new Particle(Vec2.randomCircle(MAX_DIST).toVec3().withZ(Math.random() * 5), Math.random() * .04 + .04));
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
        onRender(() -> {
            particles.forEach(p -> {
                Vec3 pos = p.pos.add(Window3D.pos.withZ(0));
                s.scale = new Vec2(p.size);
                s.draw(pos.subtract(p.pos.cross(Window3D.UP).withLength(-s.scale.x / 2)),
                        -p.pos.direction2() + Math.PI / 2, p.pos.direction() + Math.PI / 2);
            });
        });
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
