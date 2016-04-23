package game;

import engine.Core;
import engine.Input;
import engine.Signal;
import graphics.Graphics2D;
import graphics.Window3D;
import graphics.loading.SpriteContainer;
import map.CubeMap;
import static map.CubeMap.WORLD_SIZE;
import networking.Client;
import static networking.MessageType.*;
import org.lwjgl.input.Keyboard;
import static org.lwjgl.input.Keyboard.KEY_LSHIFT;
import static util.Color4.BLACK;
import util.*;

public class InvisibleMan extends RegisteredEntity {

    @Override
    protected void createInner() {
        //Create the player's variables
        Signal<Vec3> position = Premade3D.makePosition(this);
        Signal<Vec3> prevPos = Premade3D.makePrevPosition(this);
        Signal<Vec3> velocity = Premade3D.makeVelocity(this);
        Signal<Double> invincible = addChild(new Signal(5.), "invincible");

        position.set(WORLD_SIZE.multiply(.5));

        //Mutable ints
        Mutable<Integer> ammoCount = new Mutable<Integer>(3);
        Mutable<Double> moveSpeed = new Mutable<Double>(5.0);

        //Give the player basic first-person controls
        Premade3D.makeMouseLook(this, 2, -1.5, 1.5);
        Premade3D.makeWASDMovement(this, moveSpeed);
        Premade3D.makeGravity(this, new Vec3(0, 0, -15));

        //Flying cheat
//        Signal<Boolean> fly = Input.whenKey(KEY_TAB, true).reduce(false, b -> !b);
//        add(fly,
//                Input.whileKey(KEY_W, true).filter(fly).forEach(dt -> position.edit((fly.get() ? facing.toVec3() : forwards()).multiply(20 * dt)::add)),
//                Input.whileKey(KEY_S, true).filter(fly).forEach(dt -> position.edit((fly.get() ? facing.toVec3() : forwards()).multiply(-20 * dt)::add)),
//                Input.whileKey(KEY_A, true).filter(fly).forEach(dt -> position.edit(facing.toVec3().cross(UP).withLength(-20 * dt)::add)),
//                Input.whileKey(KEY_D, true).filter(fly).forEach(dt -> position.edit(facing.toVec3().cross(UP).withLength(20 * dt)::add)),
//                Input.whileKey(KEY_SPACE, true).filter(fly).forEach(dt -> position.edit(UP.multiply(20 * dt)::add)),
//                Input.whileKey(KEY_LSHIFT, true).filter(fly).forEach(dt -> position.edit(UP.multiply(-20 * dt)::add)),
//                Core.update.filter(fly).forEach(dt -> velocity.set(new Vec3(0))));
        //Make the camera automatically follow the player
        position.doForEach(v -> Window3D.pos = v.add(new Vec3(0, 0, .8)));

        //Make the player collide with the floor
        Premade3D.makeCollisions(this, new Vec3(.3, .3, .9));
        Signal<Boolean> onGround = addChild(Core.update.map(() -> velocity.get().z <= 0 && CubeMap.isSolid(position.get().add(new Vec3(0, 0, -.01)), new Vec3(.3, .3, .9))));

        //Force the player to stay inside the room
        position.filter(p -> !p.containedBy(new Vec3(0), WORLD_SIZE)).forEach(p -> {
            position.set(p.clamp(new Vec3(0), WORLD_SIZE.subtract(new Vec3(.0001))));
        });

        //Make the player slowly lose invincibility
        add(Core.update.forEach(dt -> invincible.edit(d -> d - dt)));

        //Gathering ammo
        add(Input.whenMouse(1, true).limit(.75).onEvent(() -> {
            if (ammoCount.o <= 2) {
                moveSpeed.o = moveSpeed.o * .5;
                Core.timer(.75, () -> {
                    ammoCount.o++;
                    moveSpeed.o = moveSpeed.o / .5;
                });
            }
        }));

        //Draw ammo
        Core.renderLayer(100).onEvent(() -> {
            Window3D.guiProjection();

            Graphics2D.fillRect(new Vec2(800, 50), new Vec2(300, 100), Color4.gray(.5));
            Graphics2D.drawRect(new Vec2(800, 50), new Vec2(300, 100), BLACK);
            for (int i = 0; i < ammoCount.o; i++) {
                Graphics2D.drawSprite(SpriteContainer.loadSprite("ball"), new Vec2(850 + 100 * i, 100), new Vec2(2), 0, BallAttack.BALL_COLOR);
            }

            Window3D.resetProjection();
        });

        //Throwing snowballs
        add(Input.whenMouse(0, true).limit(.5).onEvent(() -> {
            if (ammoCount.o > 0) {
                Vec3 pos = position.get().add(new Vec3(0, 0, .8));
                Vec3 vel = Window3D.facing.toVec3().withLength(30);

                Client.sendMessage(SNOWBALL, pos, vel, -1);

                BallAttack b = new BallAttack();
                b.create();
                b.get("position", Vec3.class).set(pos);
                b.get("velocity", Vec3.class).set(vel);
                ammoCount.o--;
            }

        }));

        //Jumping
        add(Input.whileKey(Keyboard.KEY_SPACE, true).filter(onGround).onEvent(() -> {
            velocity.edit(v -> v.withZ(6));
        }));

        //Creating footsteps
        //Whether you should place a left footstep or a right footstep
        Mutable<Boolean> isLeft = new Mutable(true);
        //Every .2 seconds, do the following:
        add(Core.update.filter(onGround).limit(.2).combineEventStreams(onGround.distinct()).filter(invincible.map(d -> d < 0)).onEvent(() -> {

            Vec3 pos = position.get().withZ((int) (position.get().z - .9) + .02);
            double opacity = Input.keySignal(KEY_LSHIFT).get() ? .5 : 1;

            //Create the footstep
            Footstep f = new Footstep();
            f.create();
            f.set(pos, Window3D.facing.t, isLeft.o, opacity);

            //Make the next footstep switch from left to right or vice-versa
            isLeft.o = !isLeft.o;

            //Send a message to the server with the footstep
            Client.sendMessage(FOOTSTEP, pos, Window3D.facing.t, isLeft.o, opacity);
        }));

        //Creating smoke
        add(Core.update.filter(onGround.map(b -> !b)).filter(invincible.map(d -> d < 0)).limit(.1).onEvent(() -> {
            Smoke s = new Smoke();
            s.create();
            s.get("position", Vec3.class).set(position.get().add(Vec3.randomShell(.2)));
            if (Input.keySignal(KEY_LSHIFT).get()) {
                s.get("opacity", Double.class).set(.5);
            }
            Client.sendMessage(SMOKE, s.get("position", Vec3.class).get(), s.get("opacity", Double.class).get());
        }));

    }
}
