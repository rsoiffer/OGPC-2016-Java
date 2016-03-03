package ui;

import engine.EventStream;
import engine.Input;
import engine.Signal;
import graphics.Graphics2D;
import java.util.function.Supplier;
import util.Color4;
import static util.Color4.BLACK;
import static util.Color4.TRANSPARENT;
import util.Vec2;

public class UIElement {

    public EventStream onClick = new EventStream();
    public Signal<Boolean> mouseOver = new Signal(false);
    public Vec2 size, padding = new Vec2(0);
    public Supplier<Color4> color = () -> TRANSPARENT;
    public boolean border = false;
    public Vec2 pos = new Vec2(0);

    public UIElement() {
        this(new Vec2(0));
    }

    public UIElement(Vec2 size) {
        this.size = size;
    }

    public void draw() {
        if (size.x * size.y != 0) {
            Graphics2D.fillRect(pos, size, color.get());
            if (border) {
                Graphics2D.drawRect(pos, size, BLACK);
            }
        }
    }

    public Vec2 getUL() {
        return pos.add(size.withX(0));
    }

    public void resize() {
    }

    public void setPos(Vec2 pos) {
        this.pos = pos;
    }

    public void setUL(Vec2 pos) {
        setPos(pos.subtract(size.withX(0)));
    }

    public static UIElement space(double size) {
        return new UIElement(new Vec2(size));
    }

    public static UIElement space(double x, double y) {
        return new UIElement(new Vec2(x, y));
    }

    public void update(boolean click) {
        mouseOver.set(Input.getMouse().containedBy(pos, pos.add(size)));
        if (click && mouseOver.get()) {
            onClick.sendEvent();
        }
    }
}
