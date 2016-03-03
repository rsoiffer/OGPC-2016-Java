package invisibleman;

import engine.Core;
import engine.Input;
import engine.Signal;
import graphics.Window3D;
import org.lwjgl.input.Keyboard;
import util.Mutable;
import util.RegisteredEntity;
import util.Vec3;

public class InvisibleMan extends RegisteredEntity {

    @Override
    protected void createInner() {
        //Create the player's variables
        Signal<Vec3> position = Premade3D.makePosition(this);
        Signal<Vec3> velocity = Premade3D.makeVelocity(this);
        Signal<Double> invincible = addChild(new Signal(5.), "invincible");

        //Give the player basic first-person controls
        Premade3D.makeMouseLook(this, 1, -1.5, 1.5);
        Premade3D.makeWASDMovement(this, 1);
        Premade3D.makeGravity(this, new Vec3(0,0,-5));

        //Set the initial camera position
        Window3D.pos = new Vec3(0, 0, 1);
        //Make the camera automatically follow the player
        position.forEach(v -> Window3D.pos = v.add(new Vec3(0, 0, 1)));

        //We want to keep the player inside the room
        add(Core.update.forEach(dt -> position.edit(v -> v.clamp(new Vec3(-19.75).withZ(0), new Vec3(19.75)))));
        add(Core.update.forEach(dt -> {
            if(position.get().z<=0 || position.get().z>=19.75) velocity.set(velocity.get().withZ(-0.01));
        }));
        

        //Make the player slowly lose invincibility
        add(Core.update.forEach(dt -> invincible.edit(d -> d - dt)));

        //All of the following code is for attacking
        add(Input.whenMouse(0, true).limit(.5).onEvent(() -> {
            Vec3 pos = position.get().add(new Vec3(0, 0, 1));
            Vec3 vel = Window3D.facing.toVec3().withLength(30);

            Client.sendMessage(1, pos, vel);

            BallAttack b = new BallAttack();
            b.create();
            b.get("position", Vec3.class).set(pos);
            b.get("velocity", Vec3.class).set(vel);
        }));
        
        add(Input.whenKey(Keyboard.KEY_SPACE, true).onEvent(()->{
            velocity.set(velocity.get().add(new Vec3(0,0,5)));
        }));

        //All of the following code is for creating footsteps
        //Whether you should place a left footstep or a right footstep
        Mutable<Boolean> isLeft = new Mutable(true);
        //Every .2 seconds, do the following:
        add(Core.interval(.2).filter(invincible.map(d -> d < 0)).onEvent(() -> {
            //Send a message to the server with the footstep
            Client.sendMessage(0, position.get(), Window3D.facing.t, isLeft.o);

            //Create the footstep
            Footstep f = new Footstep();
            f.create();
            //Set the footstep's position
            f.set(position.get(), Window3D.facing.t, isLeft.o);

            //Make the next footstep switch from left to right or vice-versa
            isLeft.o = !isLeft.o;
        }));
    }
}
