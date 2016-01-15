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
        //Create the footstep's variables
        Signal<Vec3> position = Premade3D.makePosition(this);
        Signal<Double> rotation = Premade3D.makeRotation(this);
        Signal<Sprite> s = Premade3D.makeSpriteGraphics(this, "footstep_white");

        //Make the footstep sink slightly so it does depth order correctly
        onUpdate(dt -> position.edit(new Vec3(0, 0, -dt / 10000)::add));

        //Opaqueness is low when the footstep is mostly transparent
        Signal<Double> opaqueness = new Signal(.5);
        s.get().color = Color4.gray(.5);
        opaqueness.forEach(d -> s.get().color = Color4.gray(1 - d));
        onUpdate(dt -> opaqueness.edit(t -> t * Math.pow(.98, dt)));

        //Destroy the footstep after 10 seconds (temporary, will change later)
        Core.timer(10, this::destroy);
    }

    //Set the footstep's variables
    public void set(Vec3 pos, double rot, boolean isLeft) {
        get("position", Vec3.class).set(pos);
        get("rotation", Double.class).set(rot);
        get("sprite", Sprite.class).get().scale = isLeft ? new Vec2(.2, .2) : new Vec2(.2, -.2);
    }
}
