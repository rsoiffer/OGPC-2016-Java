package leveleditor;

import graphics.Graphics2D;
import util.Color4;
import static util.Color4.BLACK;
import static util.Color4.GREEN;
import util.Vec2;

public class Creature extends Drawable {

    public int maxHealth = 20;
    public int health = 20;

    public Creature(Color4 color, Tile t) {
        super(color, "ball", t);
    }

    @Override
    public void draw(Vec2 pos) {
        super.draw(pos);
        Graphics2D.fillRect(pos.add(new Vec2(-20, -20)), new Vec2(40, 6), BLACK);
        Graphics2D.fillRect(pos.add(new Vec2(-20, -20)), new Vec2(40. * health / maxHealth, 6), GREEN);
        Graphics2D.drawRect(pos.add(new Vec2(-20, -20)), new Vec2(40, 6), BLACK);
    }
}
