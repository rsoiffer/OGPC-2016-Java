package invisibleman;

import engine.Core;
import engine.Input;
import engine.Signal;
import graphics.Window3D;
import map.CubeMap;
import static map.CubeMap.WORLD_SIZE;
import org.lwjgl.input.Keyboard;
import util.Mutable;
import util.RegisteredEntity;
import util.Vec3;

public class InvisibleMan extends RegisteredEntity {

    @Override
    protected void createInner() {
        //Create the player's variables
        Signal<Vec3> position = Premade3D.makePosition(this);
        Signal<Vec3> prevPos = Premade3D.makePrevPosition(this);
        Signal<Vec3> velocity = Premade3D.makeVelocity(this);
        Signal<Double> invincible = addChild(new Signal(5.), "invincible");

        position.set(WORLD_SIZE.multiply(.5));

        //Give the player basic first-person controls
        Premade3D.makeMouseLook(this, 2, -1.5, 1.5);
        Premade3D.makeWASDMovement(this, 1);
        Premade3D.makeGravity(this, new Vec3(0, 0, -10));

        //Set the initial camera position
        Window3D.pos = new Vec3(0, 0, 1);
        //Make the camera automatically follow the player
        position.forEach(v -> Window3D.pos = v.add(new Vec3(0, 0, .8)));

        //Make the player collide with the floor
        Premade3D.makeCollisions(this, new Vec3(.3, .3, .9));
        Signal<Boolean> onGround = addChild(Core.update.map(() -> CubeMap.isSolid(position.get().add(new Vec3(0, 0, -.01)), new Vec3(.3, .3, .9))));

        //Force the player to stay inside the room
        position.filter(p -> !p.containedBy(new Vec3(0), WORLD_SIZE)).forEach(p -> {
            position.set(p.clamp(new Vec3(0), WORLD_SIZE.subtract(new Vec3(.0001))));
        });

        //Make the player slowly lose invincibility
        add(Core.update.forEach(dt -> invincible.edit(d -> d - dt)));

        //Throwing snowballs
        add(Input.whenMouse(0, true).limit(.5).onEvent(() -> {
            Vec3 pos = position.get().add(new Vec3(0, 0, .8));
            Vec3 vel = Window3D.facing.toVec3().withLength(30);

            Client.sendMessage(1, pos, vel);

            BallAttack b = new BallAttack();
            b.create();
            b.get("position", Vec3.class).set(pos);
            b.get("velocity", Vec3.class).set(vel);
        }));

        //Jumping
        add(Input.whenKey(Keyboard.KEY_SPACE, true).filter(onGround).onEvent(() -> {
            velocity.edit(v -> v.withZ(4.75));
        }));

        //Creating footsteps
        //Whether you should place a left footstep or a right footstep
        Mutable<Boolean> isLeft = new Mutable(true);
        //Every .2 seconds, do the following:
        add(Core.update.filter(onGround).limit(.2).combineEventStreams(onGround.distinct()).filter(invincible.map(d -> d < 0)).onEvent(() -> {
            //Send a message to the server with the footstep
            Client.sendMessage(0, position.get().add(new Vec3(0, 0, -.88)), Window3D.facing.t, isLeft.o);

            //Create the footstep
            Footstep f = new Footstep();
            f.create();
            //Set the footstep's position
            f.set(position.get().add(new Vec3(0, 0, -.88)), Window3D.facing.t, isLeft.o);

            //Make the next footstep switch from left to right or vice-versa
            isLeft.o = !isLeft.o;
        }));
    }
}
