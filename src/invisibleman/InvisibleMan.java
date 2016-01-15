package invisibleman;

import engine.AbstractEntity;
import engine.Core;
import engine.Signal;
import graphics.Window3D;
import util.Mutable;
import util.Vec3;

public class InvisibleMan extends AbstractEntity {

    @Override
    public void create() {
        //Create the player's variables
        Signal<Vec3> position = Premade3D.makePosition(this);
        Signal<Vec3> velocity = Premade3D.makeVelocity(this);

        //Give the player basic first-person controls
        Premade3D.makeMouseLook(this, 1, -1.5, 1.5);
        Premade3D.makeWASDMovement(this, 1);

        //Set the initial camera position
        Window3D.pos = new Vec3(0, 0, 1);
        //Make the camera automatically follow the player
        position.forEach(v -> Window3D.pos = v.add(new Vec3(0, 0, 1)));

        //All the the following code is for creating footsteps
        //Whether you should place a left footstep or a right footstep
        Mutable<Boolean> isLeft = new Mutable(true);
        //Every .2 seconds, do the following:
        Core.interval(.2).onEvent(() -> {
            //Send a message to the server with the footstep
            Client.conn.sendMessage(0, position.get(), Window3D.facing.t, isLeft.o);

            //Create the footstep
            Footstep f = new Footstep();
            f.create();
            //Set the footstep's position
            f.set(position.get(), Window3D.facing.t, isLeft.o);

            //Make the next footstep switch from left to right or vice-versa
            isLeft.o = !isLeft.o;
        });
    }
}
