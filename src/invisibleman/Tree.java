package invisibleman;

import engine.AbstractEntity;
import engine.Core;
import engine.Input;
import engine.Signal;
import graphics.data.Animation;
import static org.lwjgl.input.Keyboard.KEY_A;
import static org.lwjgl.input.Keyboard.KEY_D;
import static org.lwjgl.input.Keyboard.KEY_S;
import static org.lwjgl.input.Keyboard.KEY_W;
import util.RegisteredEntity;
import util.Sounds;
import util.Vec3;

public class Tree extends AbstractEntity {

    @Override
    public void create() {
        Signal<Vec3> position = Premade3D.makePosition(this);
        Signal<Animation> animation = new Signal(new Animation("tree", "treediffuse"));

        Core.render.onEvent(() -> {
            if (RegisteredEntity.getAll(InvisibleMan.class).get(0).get("position", Vec3.class).get().subtract(position.get()).lengthSquared() < 400) {
                animation.get().draw(position.get(), 0);
            }
        });
        
        
        
        
        
        
        
//        Premade3D.makeFlatSpriteGraphics(this, "green_pinetree", Math.PI / 4).get().scale = new Vec2(2, 4);
//        Premade3D.makeFlatSpriteGraphics(this, "green_pinetree", Math.PI * 3 / 4).get().scale = new Vec2(2, 4);

        //Check for collisions with the player and play sounds
        Core.render.filter(() -> {
            InvisibleMan im = RegisteredEntity.getAll(InvisibleMan.class).get(0);
            boolean sound = im.get("invincible", Double.class).get() < 0
                    && position.get().subtract(im.get("position", Vec3.class).get()).lengthSquared() < .5
                    && (Input.keySignal(KEY_W).get() || Input.keySignal(KEY_S).get()
                    || Input.keySignal(KEY_A).get() || Input.keySignal(KEY_D).get());
            if (!sound) {
                //System.out.println("stopped");
                //Sounds.stopSound("rustle.mp3");
            }
            return sound;
        })
                .limit(2).onEvent(() -> {
            Sounds.playSound("rustle.mp3", false, 1);
        }).addChild(this);
//        Core.render.onEvent(() -> {
//            if (!Input.keySignal(KEY_W).get() && !Input.keySignal(KEY_S).get()
//                    && !Input.keySignal(KEY_A).get() && !Input.keySignal(KEY_D).get()) {
//                Sounds.stopSound("rustle.mp3");
//            }
//        });
    }
}
