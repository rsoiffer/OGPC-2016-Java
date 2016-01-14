package invisibleman;

import engine.AbstractEntity;
import engine.Core;
import engine.Signal;
import graphics.data.Sprite;
import util.Color4;
import util.Vec2;
import util.Vec3;

public class Footstep extends AbstractEntity {

    @Override
    public void create() {
        Signal<Vec3> position = Premade3D.makePosition(this);
        Premade3D.makeRotation(this);
        Signal<Sprite> s = Premade3D.makeSpriteGraphics(this, "footstep_white");
        onUpdate(dt -> position.edit(new Vec3(0, 0, -dt / 10000)::add));
        Signal<Double> opaqueness = new Signal(.5);
        opaqueness.forEach(t -> {
            s.get().color = Color4.gray(1 - t);
            //System.out.println(1 - t);
        });
        onUpdate(dt -> opaqueness.edit(t -> t * Math.pow(.98, dt)));
//            onUpdate(dt -> s.get().color = Color4.gray(1 - (1 - s.get().color.r) * Math.pow(.08, dt)));
        //onUpdate(dt -> System.out.println(s.get().color));
        Core.timer(600, this::destroy);
    }

    public void set(Vec3 pos, double rot, boolean isLeft) {
        get("position", Vec3.class).set(pos);
        get("rotation", Double.class).set(rot);
        get("sprite", Sprite.class).get().scale = isLeft ? new Vec2(.2, .2) : new Vec2(.2, -.2);
    }
}
