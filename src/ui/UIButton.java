package ui;

import engine.EventStream;
import java.util.function.Supplier;
import static util.Color4.GREEN;
import static util.Color4.RED;
import util.Vec2;

public class UIButton extends UIText {

    public EventStream onPress;

    public UIButton(Supplier<String> text, Supplier<Boolean> canPress) {
        super(text);
        onPress = onClick.filter(canPress);
        color = () -> canPress.get() ? GREEN : RED;
        padding = new Vec2(5);
        border = true;
    }
}
