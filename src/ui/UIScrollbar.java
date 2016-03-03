package ui;

import engine.Input;
import graphics.data.Framebuffer;
import graphics.data.Framebuffer.TextureAttachment;
import util.Vec2;

public class UIScrollbar extends UIElement {

    private static Framebuffer temp = new Framebuffer(new TextureAttachment()), white = new Framebuffer(new TextureAttachment());

    public Vec2 maxSize;
    public UIElement child;
    public double scroll;

    public UIScrollbar(Vec2 maxSize, UIElement child) {
        this.maxSize = maxSize;
        this.child = child;
        Input.mouseWheel.filter(mouseOver).forEach(i -> scroll += i * -.5);
    }

    @Override
    public void draw() {
        super.draw();
        child.draw();
        //temp.clear(TRANSPARENT);
        //temp.with(child::draw);
//        white.clear(TRANSPARENT);
//        white.with(() -> Graphics2D.fillRect(pos, size, WHITE));
//        white.render();
//        glBlendFunc(GL_ZERO, GL_SRC_COLOR);
//        temp.with(white::render);
//        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
//        temp.render();
//        Camera.setProjection2D(Window2D.LL(), Window2D.UR());
    }

    @Override
    public void resize() {
        child.resize();
        size = new Vec2(Math.min(child.size.x + padding.x * 2, maxSize.x), Math.min(child.size.y + padding.y * 2, maxSize.y));
        scroll = Math.max(0, Math.min(scroll, child.size.y - (size.y - padding.y * 2)));
    }

    @Override
    public void setPos(Vec2 pos) {
        super.setPos(pos);
        child.setUL(getUL().add(padding.add(new Vec2(1, -1))).add(new Vec2(0, scroll)));
    }

    @Override
    public void update(boolean click) {
        super.update(click);
        child.update(click);
    }
}
