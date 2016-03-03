package ui;

import engine.Core;
import engine.Signal;
import java.util.function.Supplier;
import static util.Color4.GREEN;

public class UICheckbox extends UIShowOne {

    public Signal<Boolean> on;

    public UICheckbox(Supplier<Boolean> canChange) {
        on = new Signal(false);

        UIImage empty = new UIImage("box");
        UIImage check = new UIImage("checkbox");
        check.sprite.color = GREEN;
        add(empty, check);

        on.doForEach(b -> showing = b ? check : empty);

        Core.delay(0, empty.onClick).filter(canChange).onEvent(() -> on.set(true));
        Core.delay(0, check.onClick).filter(canChange).onEvent(() -> on.set(false));
    }
}
