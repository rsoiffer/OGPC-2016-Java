package invisibleman;

import engine.Core;
import engine.Signal;
import graphics.data.Sprite;
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
        Signal<Sprite> s = Premade3D.makeSpriteGraphics(this, "footstep_white");
        onRender(() -> Fog.setMinTexColor(0, 0, 0, 0));

        //Make the footstep sink slightly so it does depth order correctly
        onUpdate(dt -> position.edit(new Vec3(0, 0, -dt / 10000)::add));

        //Opaqueness is low when the footstep is mostly transparent
        Signal<Double> opaqueness = Core.time().map(t -> .8 * Math.pow(.97, t));
        opaqueness.doForEach(d -> s.get().color = new Color4(1.0207 - d, 1.0207 - d, 1.0207 - d));

        //Destroy the footstep after 120 seconds (will probably change later)
        Core.timer(120, this::destroy);
    }

    //Set the footstep's variables
    public void set(Vec3 pos, double rot, boolean isLeft) {
        get("position", Vec3.class).set(pos);
        get("rotation", Double.class).set(rot);
        get("sprite", Sprite.class).get().scale = isLeft ? new Vec2(.2, .2) : new Vec2(.2, -.2);
    }
}
