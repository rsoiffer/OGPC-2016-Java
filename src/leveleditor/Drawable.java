package leveleditor;

import graphics.Graphics2D;
import graphics.loading.SpriteContainer;
import util.Color4;
import util.Vec2;

public class Drawable {

    public Color4 color;
    public String spriteName;
    public Tile tile;

    public Drawable(Color4 color, String spriteName, Tile tile) {
        this.color = color;
        this.spriteName = spriteName;
        this.tile = tile;
    }

    public void draw(Vec2 pos) {
        Graphics2D.drawSprite(SpriteContainer.loadSprite(spriteName), pos, new Vec2(1), 0, color);
    }
}
