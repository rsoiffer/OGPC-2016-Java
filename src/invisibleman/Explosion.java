package invisibleman;

import graphics.Window3D;
import graphics.data.Sprite;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import util.Color4;
import util.RegisteredEntity;
import util.Vec2;
import util.Vec3;

public class Explosion extends RegisteredEntity {

    private Vec3 pos;
    private Color4 color;

    public Explosion(Vec3 pos, Color4 color) {
        this.pos = pos;
        this.color = color;
    }

    @Override
    protected void createInner() {
        List<Particle> particles = new LinkedList();
        for (int i = 0; i < 100; i++) {
            particles.add(new Particle(pos, Vec3.randomCircle(10)));
        }
        onUpdate(dt -> {
            Iterator<Particle> it = particles.iterator();
            while (it.hasNext()) {
                Particle p = it.next();
                p.vel = p.vel.add(new Vec3(0, 0, -dt * 40));
                p.pos = p.pos.add(p.vel.multiply(dt));
                if (p.pos.z < Tile.heightAt(p.pos)) {
                    it.remove();
                }
            }
            if (particles.isEmpty()) {
                destroy();
            }
        });

        Sprite s = new Sprite("ball");
        s.color = color;
        s.scale = new Vec2(.05);
        onRender(() -> {
            particles.forEach(p -> {
                Vec3 towardsSprite = p.pos.subtract(Window3D.pos);
                s.draw(p.pos.subtract(towardsSprite.cross(Window3D.UP).withLength(-s.scale.x / 2)),
                        -towardsSprite.direction2() + Math.PI / 2, towardsSprite.direction() + Math.PI / 2);
            });
        });
    }

    private static class Particle {

        private Vec3 pos, vel;

        public Particle(Vec3 pos, Vec3 vel) {
            this.pos = pos;
            this.vel = vel;
        }
    }
}
