package ui;

import engine.Signal;
import java.util.function.Predicate;
import static ui.UIElement.space;
import static util.Color4.GREEN;
import static util.Color4.RED;

public class UIValue extends UIList {

    public Signal<Integer> value;
    public Signal<Integer> onPlus;
    public Signal<Integer> onMinus;

    public UIValue(String name, Predicate<Integer> canPlus, Predicate<Integer> canMinus) {
        value = new Signal(0);
        onPlus = new Signal(0);
        onMinus = new Signal(0);

        UIImage plus = new UIImage("plus");
        plus.mouseOver.onEvent(() -> plus.sprite.color = canPlus.test(value.get()) ? GREEN : RED);
        plus.onClick.filter(() -> canPlus.test(value.get())).onEvent(() -> {
            value.edit(i -> i + 1);
            onPlus.set(value.get());
        });

        UIImage minus = new UIImage("minus");
        minus.mouseOver.onEvent(() -> minus.sprite.color = canMinus.test(value.get()) ? GREEN : RED);
        minus.onClick.filter(() -> canMinus.test(value.get())).onEvent(() -> {
            value.edit(i -> i - 1);
            onMinus.set(value.get());
        });

        horizontal = true;
        add(new UIText(() -> name + ": " + value.get()),
                space(15),
                plus,
                space(15),
                minus);
    }
}
