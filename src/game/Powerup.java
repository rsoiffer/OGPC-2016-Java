package game;

import engine.Signal;
import graphics.Window3D;
import graphics.data.Sprite;
import java.util.function.Supplier;
import util.RegisteredEntity;
import util.Vec3;

public class Powerup extends RegisteredEntity {

    public boolean active;
    public String texture;

    @Override
    protected void createInner() {
        Premade3D.makePosition(this);

        Signal<Vec3> position = get("position", Vec3.class);
        Signal<Sprite> sprite = addChild(new Signal(new Sprite(texture)), "sprite");
        onUpdate(dt -> sprite.get().imageIndex += dt * sprite.get().imageSpeed);

        Supplier<Vec3> towardsSprite = () -> position.get().subtract(Window3D.pos);
        onRender(() -> {
            if (active) {
                sprite.get().draw(position.get().subtract(towardsSprite.get().cross(Window3D.UP).withLength(-sprite.get().scale.x / 2)),
                        Math.PI / 2 - towardsSprite.get().direction2(), towardsSprite.get().direction() + Math.PI / 2);
            }
        });
    }
}
