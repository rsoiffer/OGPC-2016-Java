package invisibleman;

import engine.AbstractEntity;
import engine.Core;
import engine.Input;
import engine.Signal;
import graphics.Window3D;
import graphics.data.Sprite;
import static gui.TypingManager.isTyping;
import java.util.function.Supplier;
import map.CubeMap;
import static org.lwjgl.input.Keyboard.*;
import org.lwjgl.input.Mouse;
import util.Util;
import util.Vec2;
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

    public static Signal<Vec3> makePrevPosition(AbstractEntity e) {
        Signal<Vec3> position = e.get("position", Vec3.class);
        Signal<Vec3> prevPos = e.addChild(new Signal(ZERO), "prevPos");
        e.onUpdate(dt -> prevPos.set(position.get()));
        return prevPos;
    }

    public static Signal<Double> makeRotation(AbstractEntity e) {
        return e.addChild(new Signal(0.), "rotation");
    }

    public static Signal<Vec3> makeVelocity(AbstractEntity e) {
        Signal<Vec3> position = e.get("position", Vec3.class);
        return e.addChild(Core.update.collect(ZERO, (v, dt) -> position.edit(v.multiply(dt)::add)), "velocity");
    }

    public static void makeWASDMovement(AbstractEntity e, double maxSpeed) {
        Signal<Boolean> blink = new Signal(false);
        Input.whenKey(KEY_B, true).onEvent(() -> blink.set(true));
        Input.whenKey(KEY_B, true).throttle(.1).onEvent(() -> blink.set(false));
        Supplier<Double> speed = () -> maxSpeed * (Input.keySignal(KEY_LSHIFT).get() ? .5 : 1) * (blink.get() ? 20 : 1);
        Signal<Vec3> velocity = e.get("velocity", Vec3.class);
        e.onUpdate(dt -> {

            if (!isTyping()) {

                Vec2 dir = new Vec2(0);
                if (Input.keySignal(KEY_W).get()) {
                    dir = dir.add(new Vec2(1, 0));
                }
                if (Input.keySignal(KEY_S).get()) {
                    dir = dir.add(new Vec2(-1, 0));
                }
                if (Input.keySignal(KEY_A).get()) {
                    dir = dir.add(new Vec2(0, 1));
                }
                if (Input.keySignal(KEY_D).get()) {
                    dir = dir.add(new Vec2(0, -1));
                }
                if (dir.equals(new Vec2(0))) {
                    velocity.set(new Vec3(0, 0, velocity.get().z));
                } else {
                    if (dir.equals(new Vec2(1, 0))) {
                        dir = dir.withLength(speed.get());
                    } else {
                        dir = dir.withLength(speed.get() * .75);
                    }
                    dir = dir.rotate(Window3D.facing.t);
                    velocity.set(new Vec3(dir.x, dir.y, velocity.get().z));
                }
            }
        });
//        e.onUpdate(dt -> velocity.set(ZERO.withZ(velocity.get().z)));
//        //Supplier<Boolean> onlyW = () -> Input.keySignal(KEY_W).get() && !Input.keySignal(KEY_S).get() && !Input.keySignal(KEY_A).get() && !Input.keySignal(KEY_D).get();
//
//        e.add(Input.whileKey(KEY_W, true).filter(new Signal(Mouse.isGrabbed())).forEach(dt
//                -> velocity.edit(Window3D.forwards().multiply(speed.get())::add)),
//                Input.whileKey(KEY_S, true).filter(new Signal(Mouse.isGrabbed())).forEach(dt
//                -> velocity.edit(Window3D.forwards().multiply(-speed.get())::add)),
//                Input.whileKey(KEY_A, true).filter(new Signal(Mouse.isGrabbed())).forEach(dt
//                -> velocity.edit(Window3D.UP.cross(Window3D.forwards()).multiply(speed.get())::add)),
//                Input.whileKey(KEY_D, true).filter(new Signal(Mouse.isGrabbed())).forEach(dt
//                -> velocity.edit(Window3D.UP.cross(Window3D.forwards()).multiply(-speed.get())::add)));
//        e.onUpdate(dt -> {
//            if (velocity.get().toVec2().lengthSquared() > speed.get() * speed.get()) {
//                velocity.edit(v -> v.toVec2().withLength(speed.get()).toVec3().withZ(v.z));
//            }
//        });
    }

    public static Signal<Vec3> makeCollisions(AbstractEntity e, Vec3 size) {
        Signal<Vec3> position = e.get("position", Vec3.class);
        Signal<Vec3> prevPos = e.get("prevPos", Vec3.class);
        Signal<Vec3> velocity = e.get("velocity", Vec3.class);
        Signal<Vec3> collision = new Signal(null);
        e.onUpdate(dt -> {
            if (CubeMap.isSolid(position.get(), size)) {
                Vec3 pos = position.get();
                int detail = 20;
                Vec3 delta = position.get().subtract(prevPos.get()).divide(detail);
                position.set(prevPos.get());
                for (int i = 0; i < detail; i++) {
                    position.edit(delta.withY(0).withZ(0)::add);
                    if (CubeMap.isSolid(position.get(), size)) {
                        position.edit(delta.withY(0).withZ(0).reverse()::add);
                        velocity.edit(v -> v.withX(0));
                        break;
                    }
                }
                for (int i = 0; i < detail; i++) {
                    position.edit(delta.withX(0).withZ(0)::add);
                    if (CubeMap.isSolid(position.get(), size)) {
                        position.edit(delta.withX(0).withZ(0).reverse()::add);
                        velocity.edit(v -> v.withY(0));
                        break;
                    }
                }
                for (int i = 0; i < detail; i++) {
                    position.edit(delta.withY(0).withX(0)::add);
                    if (CubeMap.isSolid(position.get(), size)) {
                        position.edit(delta.withY(0).withX(0).reverse()::add);
                        velocity.edit(v -> v.withZ(0));
                        break;
                    }
                }
                collision.set(pos);
            }
        });
        return collision;
    }

    //Graphics
    public static void makeMouseLook(AbstractEntity e, double sensitivity, double min, double max) {
        e.onUpdate(dt -> {
            Window3D.facing = Mouse.isGrabbed() ? (new Vec3Polar(1, Window3D.facing.t + -sensitivity * Input.getMouseDelta().x,
                    Util.clamp(Window3D.facing.p + sensitivity * Input.getMouseDelta().y, min, max))) : Window3D.facing;
        });
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
