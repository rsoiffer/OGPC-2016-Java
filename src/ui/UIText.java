package ui;

import graphics.Graphics2D;
import graphics.loading.FontContainer;
import java.awt.Font;
import java.util.function.Supplier;
import org.newdawn.slick.Color;
import util.Vec2;

public class UIText extends UIElement {

    static {
        FontContainer.add("Small", "Cambria", Font.PLAIN, 16);
        FontContainer.add("Medium", "Cambria", Font.PLAIN, 18);
    }

    public Supplier<String> text;
    public String font = "Default";

    public UIText(Supplier<String> text) {
        this.text = text;
    }

    @Override
    public void draw() {
        super.draw();
        Graphics2D.drawText(text.get(), font, getUL().add(padding.multiply(new Vec2(1, -1))), Color.black, (int) (size.x - padding.x * 2));
    }

    @Override
    public void resize() {
        double width = Graphics2D.getTextWidth(text.get(), font) + 1;
        size = size.withX(Math.min(width, 600));

        double height = Graphics2D.getTextHeight(text.get(), font, (int) size.x);
        size = size.withY(height);

        size = size.add(padding.multiply(2));
    }

    public static UIText text(String text) {
        return new UIText(() -> text);
    }

    public static UIText text(String text, String font) {
        UIText r = new UIText(() -> text);
        r.font = font;
        return r;
    }

    @Override
    public String toString() {
        return "Text: " + text.get();
    }
}
