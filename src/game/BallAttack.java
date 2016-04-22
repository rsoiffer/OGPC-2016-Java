package game;

import engine.Core;
import engine.Signal;
import graphics.data.Sprite;
import networking.Client;
import static networking.MessageType.HIT;
import util.Color4;
import util.RegisteredEntity;
import util.Vec2;
import util.Vec3;

public class BallAttack extends RegisteredEntity {
    
    private static Color4 BALL_COLOR = new Color4(0,.5,1);

    public boolean isEnemy;
    public int thrower;

    @Override
    protected void createInner() {
        //Create the ball's variables
        Signal<Vec3> position = Premade3D.makePosition(this);
        Signal<Vec3> prevPos = Premade3D.makePrevPosition(this);
        Signal<Vec3> velocity = Premade3D.makeVelocity(this);
        Signal<Vec3> gravity = Premade3D.makeGravity(this, new Vec3(0, 0, -30));
        Signal<Sprite> sprite = Premade3D.makeFacingSpriteGraphics(this, "ball");
        sprite.get().color = BALL_COLOR;
        sprite.get().scale = new Vec2(.1);

        
        //Check for collisions with the player
        Core.update.filter(dt -> isEnemy).forEach(dt -> RegisteredEntity.getAll(InvisibleMan.class).forEach(im -> {
            if (im.get("invincible", Double.class).get() < 0) {
                if (position.get().subtract(im.get("position", Vec3.class).get()).lengthSquared() < 2) {
                    im.destroy();
                    new InvisibleMan().create();
                    Client.sendMessage(HIT, position.get(), thrower);
                }
            }
        })).addChild(this);
        
        //Trail particles
        add(Core.interval(.02).onEvent(() -> Particle.addParticle(4, () -> new Particle(position.get(), Vec3.randomCircle(1), new Vec3(0), 1, .02, BALL_COLOR))));


        //Destroy the ball when it hits the ground
        Premade3D.makeCollisions(this, new Vec3(0)).onEvent(() -> {
            destroy();
            Particle.explode(position.get(),BALL_COLOR);
        });
        
    }
}
