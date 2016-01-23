package invisibleman;

import engine.Core;
import engine.Signal;
import graphics.data.Sprite;
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

        //Destroy the ball when it hits the ground
        position.filter(v -> v.z < 0).onEvent(() -> {
            destroy();
            new Explosion(position.get().withZ(0), new Color4(0, .5, 100)).create();
        });
    }
}
