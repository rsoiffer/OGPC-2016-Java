package invisibleman;

import engine.Core;
import engine.Input;
import engine.Signal;
import graphics.Window3D;
import org.lwjgl.input.Keyboard;
import util.Mutable;
import util.RegisteredEntity;
import util.Vec2;
import util.Vec3;

public class InvisibleMan extends RegisteredEntity {

    @Override
    protected void createInner() {
        //Create the player's variables
        Signal<Vec3> position = Premade3D.makePosition(this);
        Signal<Vec3> prevPos = addChild(new Signal(new Vec3(0)));
        Signal<Vec3> velocity = Premade3D.makeVelocity(this);
        Signal<Double> invincible = addChild(new Signal(5.), "invincible");

        position.set(Tile.size().divide(2).toVec3().withZ(Tile.heightAt(Tile.size().divide(2).toVec3())));

        //Give the player basic first-person controls
        Premade3D.makeMouseLook(this, 2, -1.5, 1.5);
        Premade3D.makeWASDMovement(this, 1);
        Premade3D.makeGravity(this, new Vec3(0, 0, -10));

        //Set the initial camera position
        Window3D.pos = new Vec3(0, 0, 1);
        //Make the camera automatically follow the player
        position.forEach(v -> Window3D.pos = v.add(new Vec3(0, 0, 1)));

        //Make the player collide with the floor
        Signal<Boolean> onGround = new Signal(true);
        onUpdate(dt -> {
            if (position.get().z <= Tile.heightAt(position.get()) + 2 * dt) {
                if (velocity.get().toVec2().dot(Tile.normalAt(position.get())) > 0 && Tile.normalAt(position.get()).lengthSquared() > 4) {
                    position.set(prevPos.get());
                }
                position.edit(p -> p.withZ(Tile.heightAt(position.get())));
                velocity.edit(v -> v.withZ(0));
                onGround.set(true);
            } else {
                onGround.set(false);
            }
            position.set(position.get().toVec2().clamp(new Vec2(0), Tile.size()).toVec3().withZ(position.get().z));
            prevPos.set(position.get());
        });

        //Force the player to stay inside the room
        position.filter(p -> !p.toVec2().containedBy(new Vec2(0), Tile.size().subtract(new Vec2(Tile.TILE_SIZE)))).forEach(p -> {
            position.set(p.toVec2().clamp(new Vec2(0), Tile.size().subtract(new Vec2(Tile.TILE_SIZE + .0001))).toVec3().withZ(p.z));
        });

        //Make the player slowly lose invincibility
        add(Core.update.forEach(dt -> invincible.edit(d -> d - dt)));

        //Throwing snowballs
        add(Input.whenMouse(0, true).limit(.5).onEvent(() -> {
            Vec3 pos = position.get().add(new Vec3(0, 0, 1));
            Vec3 vel = Window3D.facing.toVec3().withLength(30);

            Client.sendMessage(1, pos, vel);

            BallAttack b = new BallAttack();
            b.create();
            b.get("position", Vec3.class).set(pos);
            b.get("velocity", Vec3.class).set(vel);
        }));

        //Jumping
        add(Input.whenKey(Keyboard.KEY_SPACE, true).filter(onGround).onEvent(() -> {
            velocity.set(velocity.get().add(new Vec3(0, 0, 4)));
            onGround.set(false);
        }));

        //Creating footsteps
        //Whether you should place a left footstep or a right footstep
        Mutable<Boolean> isLeft = new Mutable(true);
        //Every .2 seconds, do the following:
        add(Core.update.filter(onGround).limit(.2).combineEventStreams(onGround.distinct()).filter(invincible.map(d -> d < 0)).onEvent(() -> {
            //Send a message to the server with the footstep
            Client.sendMessage(0, position.get().add(new Vec3(0, 0, .02)), Window3D.facing.t, isLeft.o);

            //Create the footstep
            Footstep f = new Footstep();
            f.create();
            //Set the footstep's position
            f.set(position.get().add(new Vec3(0, 0, .02)), Window3D.facing.t, isLeft.o);

            //Make the next footstep switch from left to right or vice-versa
            isLeft.o = !isLeft.o;
        }));
    }
}
