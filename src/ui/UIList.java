package ui;

import graphics.Graphics2D;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import util.Color4;
import util.Vec2;

public class UIList extends UIElement {

    public List<UIElement> parts = new LinkedList();
    public boolean horizontal;
    public double gravity = 0;
    public boolean listBorders;

    public void add(UIElement... a) {
        parts.addAll(Arrays.asList(a));
    }

    @Override
    public void draw() {
        super.draw();
        parts.forEach(UIElement::draw);
        if (listBorders) {
            double along = horizontal ? padding.x : padding.y;
            for (int i = 0; i < parts.size() - 1; i++) {
                along += horizontal ? parts.get(i).size.x : parts.get(i).size.y;
                if (horizontal) {
                    Graphics2D.drawLine(new Vec2(pos.x + along, pos.y), new Vec2(pos.x + along, pos.y + size.y));
                } else {
                    Graphics2D.drawLine(new Vec2(pos.x, getUL().y - along), new Vec2(pos.x + size.x, getUL().y - along));
                }
            }
        }
    }

    public static UIList list(boolean horizontal, UIElement... a) {
        UIList r = new UIList();
        r.horizontal = horizontal;
        r.add(a);
        return r;
    }

    @Override
    public void resize() {
        parts.forEach(UIElement::resize);
        double width, height;
        if (!horizontal) {
            width = parts.stream().mapToDouble(e -> e.size.x).max().orElse(0);
            height = parts.stream().mapToDouble(e -> e.size.y).sum();
        } else {
            width = parts.stream().mapToDouble(e -> e.size.x).sum();
            height = parts.stream().mapToDouble(e -> e.size.y).max().orElse(0);
        }
        size = new Vec2(width, height);
//        parts.forEach(e -> {
//            if (!horizontal) {
//                e.size = e.size.withX(width);
//            } else {
//                e.size = e.size.withY(height);
//            }
//        });
        size = size.add(padding.multiply(2));
    }

    public void setAllBorders(boolean border) {
        parts.forEach(e -> e.border = border);
    }

    public void setAllColors(Supplier<Color4> color) {
        parts.forEach(e -> e.color = color);
    }

    public void setAllPadding(Vec2 padding) {
        parts.forEach(e -> e.padding = padding);
    }

    @Override
    public void setPos(Vec2 pos) {
        super.setPos(pos);
        double amt = parts.stream().mapToDouble(e -> horizontal ? e.size.y : e.size.x).max().orElse(0);
        Vec2 along = padding.multiply(new Vec2(1, -1));
        for (UIElement e : parts) {
            double missing = (horizontal ? e.size.y : e.size.x) - amt;
            Vec2 offset = (horizontal ? new Vec2(0, 1) : new Vec2(-1, 0)).multiply(missing * gravity);
            e.setUL(getUL().add(along).add(offset));
            along = along.add(horizontal ? e.size.withY(0) : e.size.withX(0).reverse());
        }
    }

    @Override
    public void update(boolean click) {
        super.update(click);
        parts.forEach(e -> e.update(click));
    }
}
