package ui;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import util.Vec2;

public class UIShowOne extends UIElement {

    public UIElement showing = new UIElement();
    public List<UIElement> parts = new LinkedList();

    public void add(UIElement... a) {
        parts.addAll(Arrays.asList(a));
    }

    @Override
    public void draw() {
        super.draw();
        showing.draw();
    }

    @Override
    public void resize() {
        parts.forEach(UIElement::resize);
        size = showing.size.add(padding.multiply(2));
    }

    @Override
    public void setPos(Vec2 pos) {
        super.setPos(pos);
        parts.forEach(e -> e.setUL(getUL().add(padding.multiply(new Vec2(1, -1)))));
    }

    public void show(UIElement e) {
        if (!parts.contains(e)) {
            parts.add(e);
        }
        showing = e;
    }

    @Override
    public void update(boolean click) {
        super.update(click);
        showing.update(click);
    }
}
