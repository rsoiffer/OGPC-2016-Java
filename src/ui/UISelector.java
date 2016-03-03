package ui;

import engine.Core;
import engine.Signal;
import java.util.List;
import java.util.function.Supplier;
import static ui.UIText.text;
import util.Color4;
import util.Vec2;

public class UISelector extends UIShowOne {

    public List<String> options;
    public Signal<String> chosen;
    public boolean showOptions;

    public UISelector(String c, List<String> o, Supplier<Boolean> canPick) {
        chosen = new Signal(c);
        options = o;

        UIText title = text(c);
        UIList list = new UIList();
        title.border = list.border = true;
        title.color = list.color = () -> Color4.gray(.9);
        title.padding = list.padding = new Vec2(5);
        options.forEach(s -> {
            UIText text = text(s);
            list.add(text);
            Core.delay(0, text.onClick).onEvent(() -> {
                chosen.set(s);
                showing = title;
                title.text = () -> s;
            });
        });
        add(title, list);

        showing = title;
        Core.delay(0, onClick).filter(canPick).onEvent(() -> {
            if (!showOptions) {
                showing = list;
            }
        });
    }
}
