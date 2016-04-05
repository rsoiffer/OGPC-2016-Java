package invisibleman;

import engine.Core;
import engine.Signal;
import graphics.Graphics3D;
import graphics.data.Sprite;
import java.util.ArrayList;
import java.util.List;
import util.Color4;
import util.RegisteredEntity;
import util.Vec2;
import util.Vec3;

public class BallAttack extends RegisteredEntity {

    public boolean isEnemy;

    @Override
    protected void createInner() {
        //Create the ball's variables
        Signal<Vec3> position = Premade3D.makePosition(this);
        Signal<Vec3> prevPos = Premade3D.makePrevPosition(this);
        Signal<Vec3> velocity = Premade3D.makeVelocity(this);
        Signal<Vec3> gravity = Premade3D.makeGravity(this, new Vec3(0, 0, -50));
        Signal<Sprite> sprite = Premade3D.makeFacingSpriteGraphics(this, "ball");
        sprite.get().color = new Color4(0, .5, 1);
        sprite.get().scale = new Vec2(.1);

        //Check for collisions with the player
        Core.update.filter(dt -> isEnemy).forEach(dt -> RegisteredEntity.getAll(InvisibleMan.class).forEach(im -> {
            if (im.get("invincible", Double.class).get() < 0) {
                if (position.get().subtract(im.get("position", Vec3.class).get()).lengthSquared() < .5) {
                    //im.get("position", Vec3.class).set(ZERO);
                    im.destroy();
                    new InvisibleMan().create();
                    Client.sendMessage(2, position.get());
                }
            }
        })).addChild(this);

        Signal<List<Vec3>> pastPos = position.collect(new ArrayList(), List::add);
        onRender(() -> {
            Fog.setMinTexColor(1, 1, 1, 1);
            for (int i = 0; i < pastPos.get().size() - 1; i++) {
                Graphics3D.drawLine(pastPos.get().get(i), pastPos.get().get(i + 1), new Color4(0, .5, 1, .6));
            }
            Fog.setMinTexColor(0, 0, 0, 0);
        });

        //Destroy the ball when it hits the ground
        Premade3D.makeCollisions(this, new Vec3(0)).onEvent(() -> {
            destroy();
            new Explosion(position.get(), new Color4(0, .5, 1)).create();
        });
    }
}
