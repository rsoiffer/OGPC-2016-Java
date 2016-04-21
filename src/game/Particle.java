package game;

import engine.Core;
import graphics.Window3D;
import graphics.data.Sprite;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import map.CubeMap;
import util.Color4;
import util.Util;
import util.Vec2;
import util.Vec3;

public class Particle {

    private static final List<Particle> ALL = new LinkedList();

    public static void addParticle(int num, Supplier<Particle> s) {
        Util.repeat(num, () -> ALL.add(s.get()));
    }

    public static void clear() {
        ALL.clear();
    }

    public static void explode(Vec3 pos, Color4 color) {
        addParticle(200, () -> new Particle(pos, Vec3.randomCircle(15), new Vec3(0, 0, -40), Double.POSITIVE_INFINITY, .05, color));
    }

    static {
        Core.update.forEach(dt -> {
            Iterator<Particle> it = ALL.iterator();
            while (it.hasNext()) {
                Particle p = it.next();
                p.vel = p.vel.add(p.gra.multiply(dt));
                p.pos = p.pos.add(p.vel.multiply(dt));
                p.lifeTime -= dt;
                if (p.lifeTime < 0 || CubeMap.isSolid(p.pos)) {
                    it.remove();
                }
            }
        });
        Sprite s = new Sprite("ball");
        Core.render.onEvent(() -> {
            ALL.forEach(p -> {
                s.scale = new Vec2(p.size);
                s.color = p.color;
                Vec3 towardsSprite = p.pos.subtract(Window3D.pos);
                s.draw(p.pos.subtract(towardsSprite.cross(Window3D.UP).withLength(-s.scale.x / 2)),
                        -towardsSprite.direction2() + Math.PI / 2, towardsSprite.direction() + Math.PI / 2);
            });
        });
    }

    public Particle(Vec3 pos, Vec3 vel, Vec3 gra, double lifeTime, double size, Color4 color) {
        this.pos = pos;
        this.vel = vel;
        this.gra = gra;
        this.lifeTime = lifeTime;
        this.size = size;
        this.color = color;
    }

    public Vec3 pos;
    public Vec3 vel;
    public Vec3 gra;
    public double lifeTime;
    public double size;
    public Color4 color;
}
