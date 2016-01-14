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

        Signal<Vec3> position = Premade3D.makePosition(this);
        Signal<Vec3> velocity = Premade3D.makeVelocity(this);
        Premade3D.makeMouseLook(this, 1, -1.5, 1.5);
        Premade3D.makeWASDMovement(this, 1);

        Window3D.pos = new Vec3(0, 0, 1);
        position.forEach(v -> Window3D.pos = v.add(new Vec3(0, 0, 1)));

        Mutable<Boolean> isLeft = new Mutable(true);
        Core.interval(.2).onEvent(() -> {
            ClientMain.conn.sendMessage(0, position.get(), Window3D.facing.t, isLeft.o);

            Footstep f = new Footstep();
            f.create();
            f.set(position.get(), Window3D.facing.t, isLeft.o);
            isLeft.o = !isLeft.o;
        });
    }
}
