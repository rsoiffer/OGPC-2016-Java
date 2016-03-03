package ui;

import graphics.data.Sprite;
import util.Vec2;

public class UIImage extends UIElement {

    public Sprite sprite;

    public UIImage(String sprite) {
        this.sprite = new Sprite(sprite);
    }

    @Override
    public void draw() {
        super.draw();
        sprite.draw(pos.add(size.multiply(.5)), 0);
    }

    @Override
    public void resize() {
        size = new Vec2(sprite.getTexture().getImageWidth(), sprite.getTexture().getImageHeight());
    }
}
