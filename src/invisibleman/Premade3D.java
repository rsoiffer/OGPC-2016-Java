package invisibleman;

import engine.AbstractEntity;
import engine.Core;
import engine.Input;
import engine.Signal;
import graphics.Window3D;
import graphics.data.Sprite;
import java.util.function.Supplier;
import static org.lwjgl.input.Keyboard.*;
import util.Mutable;
import util.Util;
import util.Vec3;
import static util.Vec3.ZERO;
import util.Vec3Polar;

public abstract class Premade3D {

    //Movement
    public static Signal<Vec3> makeGravity(AbstractEntity e, Vec3 g) {
        Signal<Vec3> velocity = e.get("velocity", Vec3.class);
        return e.addChild(Core.update.collect(g, (v, dt) -> velocity.edit(v.multiply(dt)::add)), "gravity");
    }

    public static Signal<Vec3> makePosition(AbstractEntity e) {
        return e.addChild(new Signal(ZERO), "position");
    }

    public static Signal<Double> makeRotation(AbstractEntity e) {
        return e.addChild(new Signal(0.), "rotation");
    }

    public static Signal<Vec3> makeVelocity(AbstractEntity e) {
        Signal<Vec3> position = e.get("position", Vec3.class);
        return e.addChild(Core.update.collect(ZERO, (v, dt) -> position.edit(v.multiply(dt)::add)), "velocity");
    }

    public static void makeWASDMovement(AbstractEntity e, double sped) {
        Mutable<Double> speed = new Mutable(sped);
        Input.whenKey((KEY_LSHIFT), true).onEvent(() -> {
            speed.o*=1.5;
        });
        Signal<Vec3> velocity = e.get("velocity", Vec3.class);
        e.onUpdate(dt -> velocity.set(ZERO.withZ(velocity.get().z)));
        Supplier<Boolean> onlyW = () -> Input.keySignal(KEY_W).get() && !Input.keySignal(KEY_S).get() && !Input.keySignal(KEY_A).get() && !Input.keySignal(KEY_D).get();
        e.add(Input.whileKeyDown(KEY_W).forEach(dt -> velocity.edit(Window3D.forwards().multiply((onlyW.get() ? 2 : 1) * speed.o)::add)),
                Input.whileKeyDown(KEY_S).forEach(dt -> velocity.edit(Window3D.forwards().multiply(-speed.o)::add)),
                Input.whileKeyDown(KEY_A).forEach(dt -> velocity.edit(Window3D.UP.cross(Window3D.forwards()).multiply(speed.o)::add)),
                Input.whileKeyDown(KEY_D).forEach(dt -> velocity.edit(Window3D.UP.cross(Window3D.forwards()).multiply(-speed.o)::add)));
    }

    //Graphics
    public static void makeMouseLook(AbstractEntity e, double sensitivity, double min, double max) {
        e.onUpdate(dt -> Window3D.facing = new Vec3Polar(1, Window3D.facing.t + -sensitivity * Input.getMouseDelta().x,
                Util.clamp(Window3D.facing.p + sensitivity * Input.getMouseDelta().y, min, max)));
    }

    public static Signal<Sprite> makeSpriteGraphics(AbstractEntity e, String name) {
        Signal<Vec3> position = e.get("position", Vec3.class);
        Supplier<Double> rotation = e.getOrDefault("rotation", () -> 0.);
        Signal<Sprite> sprite = e.addChild(new Signal(new Sprite(name)), "sprite");
        e.onUpdate(dt -> sprite.get().imageIndex += dt * sprite.get().imageSpeed);
        e.onRender(() -> sprite.get().draw(position.get(), 0, rotation.get()));
        return sprite;
    }

    public static Signal<Sprite> makeFlatSpriteGraphics(AbstractEntity e, String name, double angle) {
        Signal<Vec3> position = e.get("position", Vec3.class);
        Signal<Sprite> sprite = e.addChild(new Signal(new Sprite(name)), "sprite");
        e.onUpdate(dt -> sprite.get().imageIndex += dt * sprite.get().imageSpeed);
        e.onRender(() -> sprite.get().draw(position.get().subtract(new Vec3Polar(sprite.get().scale.x / 2, angle, 0).toVec3()), Math.PI / 2, angle));
        return sprite;
    }

    public static Signal<Sprite> makeFlatFacingSpriteGraphics(AbstractEntity e, String name) {
        Signal<Vec3> position = e.get("position", Vec3.class);
        Signal<Sprite> sprite = e.addChild(new Signal(new Sprite(name)), "sprite");
        e.onUpdate(dt -> sprite.get().imageIndex += dt * sprite.get().imageSpeed);

        Supplier<Vec3> towardsSprite = () -> position.get().subtract(Window3D.pos);
        e.onRender(() -> sprite.get().draw(position.get().subtract(towardsSprite.get().cross(Window3D.UP).withLength(-sprite.get().scale.x / 2)),
                Math.PI / 2, towardsSprite.get().direction() + Math.PI / 2));
        return sprite;
    }

    public static Signal<Sprite> makeFacingSpriteGraphics(AbstractEntity e, String name) {
        Signal<Vec3> position = e.get("position", Vec3.class);
        Signal<Sprite> sprite = e.addChild(new Signal(new Sprite(name)), "sprite");
        e.onUpdate(dt -> sprite.get().imageIndex += dt * sprite.get().imageSpeed);

        Supplier<Vec3> towardsSprite = () -> position.get().subtract(Window3D.pos);
        e.onRender(() -> sprite.get().draw(position.get().subtract(towardsSprite.get().cross(Window3D.UP).withLength(-sprite.get().scale.x / 2)),
                Math.PI / 2 - towardsSprite.get().direction2(), towardsSprite.get().direction() + Math.PI / 2));
        return sprite;
    }
}
