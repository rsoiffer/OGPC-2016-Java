/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package invisibleman;

import engine.AbstractEntity;
import engine.Signal;
import graphics.Window3D;
import graphics.data.Sprite;
import java.util.function.Supplier;
import util.Vec3;

/**
 *
 * @author gvandomelen19
 */
public class Tree extends AbstractEntity {
    
    public void create() {
        Signal<Vec3> position = Premade3D.makePosition(this);
        Signal<Double> rotation = Premade3D.makeRotation(this);
        Signal<Sprite> s = makeTreeGraphics(this, "green_pinetree");
    }
    
    public static Signal<Sprite> makeTreeGraphics(AbstractEntity e, String name) {
        Signal<Vec3> position = e.get("position", Vec3.class);
        Supplier<Double> rotation = e.getOrDefault("rotation", () -> 0.);
        Signal<Sprite> sprite = e.addChild(new Signal(new Sprite(name)), "sprite");
        e.onUpdate(dt -> sprite.get().imageIndex += dt * sprite.get().imageSpeed);
        e.onRender(() -> sprite.get().draw(position.get(), Math.PI/2, Window3D.facing.t+Math.PI/2));
        return sprite;
    }
    
}
