package leveleditor;

import engine.Core;
import engine.Input;
import engine.Signal;
import graphics.Window2D;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import org.lwjgl.input.Keyboard;
import ui.UIElement;
import static ui.UIElement.space;
import ui.UIList;
import static ui.UIList.list;
import ui.UISelector;
import ui.UIShowOne;
import ui.UIText;
import static ui.UIText.text;
import ui.UIValue;
import util.Color4;
import static util.Color4.BLACK;
import static util.Color4.BLUE;
import static util.Color4.GREEN;
import static util.Color4.ORANGE;
import static util.Color4.PURPLE;
import static util.Color4.RED;
import static util.Color4.WHITE;
import static util.Color4.YELLOW;
import util.Util;
import util.Vec2;

public class LevelEditor {

    private static Supplier<Boolean> notOverUI;
    private static int uiMode;
    private static Color4 serverColor = Color4.random();

    public static void main(String[] args) {
        //Game
        Core.init();

        Tile.init();
        UI();

        Input.whenKey(Keyboard.KEY_C, true).onEvent(() -> serverColor = Color4.random());

        Input.whenKey(Keyboard.KEY_O, true).onEvent(() -> {
            try {
                PrintWriter writer = new PrintWriter("level.txt", "UTF-8");
                Util.forRange(0, 50, 0, 50, (x, y) -> {
                    Tile t = Tile.grid[x][y];
                    if (t.drawable != null) {
                        writer.println(x + " " + y + " " + t.color + " | " + t.drawable.spriteName + " " + t.drawable.color);
                        //+ (t.drawable instanceof Creature) ? (" " + ((Creature) t.drawable).health) : "");
                    } else {
                        writer.println(x + " " + y + " " + t.color);
                    }
                    //System.out.println(x + " " + y);
                });
                writer.close();
            } catch (Exception ex) {
            }
        });
        Input.whenKey(Keyboard.KEY_L, true).onEvent(() -> {
            try {
                Files.readAllLines(Paths.get("level.txt")).forEach(s -> {
                    int x = Integer.parseInt(s.substring(0, s.indexOf(" ")));
                    s = s.substring(s.indexOf(" ") + 1);
                    int y = Integer.parseInt(s.substring(0, s.indexOf(" ")));
                    //System.out.println(x + " " + y);
                    Tile t = Tile.grid[x][y];
                    String[] color = s.substring(s.indexOf("[") + 1, s.indexOf("]")).split(", ");
                    t.color = new Color4(Double.parseDouble(color[0]), Double.parseDouble(color[1]),
                            Double.parseDouble(color[2]), Double.parseDouble(color[3]));
                    if (s.contains("|")) {
                        s = s.substring(s.indexOf("| ") + 2);
                        String spriteName = s.substring(0, s.indexOf(" "));
                        String[] color2 = s.substring(s.indexOf("[") + 1, s.indexOf("]")).split(", ");
                        Color4 c = new Color4(Double.parseDouble(color2[0]), Double.parseDouble(color2[1]),
                                Double.parseDouble(color2[2]), Double.parseDouble(color2[3]));
                        if (spriteName.equals("ball")) {
                            t.drawable = new Creature(c, t);
                        } else {
                            t.drawable = new Drawable(c, spriteName, t);
                        }
                    } else {
                        t.drawable = null;
                    }
                });
            } catch (Exception ex) {
            }
        });

        Core.run();

    }

    private static void UI() {
        //Loop
        UIShowOne screen = new UIShowOne();
        notOverUI = screen.mouseOver.map(b -> !b);
        Signal<Boolean> clicked = Input.whenMouse(0, true).combineEventStreams(Input.whileMouseDown(1).limit(.05)).map(() -> true);
        Core.renderLayer(1).onEvent(() -> {
            screen.resize();
            screen.setUL(new Vec2(-600, 400).add(Window2D.viewPos));
            screen.update(clicked.get());
            screen.resize();
            screen.setUL(new Vec2(-600, 400).add(Window2D.viewPos));
            screen.draw();
            clicked.set(false);
        });

        //Create ui
        UIElement tileUI = tileUI();
        UIElement creatureUI = creatureUI();
        screen.add(tileUI, creatureUI);
        screen.color = () -> Color4.gray(.95);
        screen.border = true;

        //Switch mode
        Input.whenKey(Keyboard.KEY_SPACE, true).onEvent(() -> {
            uiMode = (uiMode + 1) % 3;
            switch (uiMode) {
                case 0:
                    screen.showing = new UIElement();
                    break;
                case 1:
                    screen.showing = tileUI;
                    break;
                case 2:
                    screen.showing = creatureUI;
                    break;
            }
        });
    }

    private static UIElement creatureUI() {
        UIText name = text("Select an object");

        Signal<Drawable> selected = new Signal(null);
        UIElement selectedView = new UIElement(new Vec2(50)) {
            @Override
            public void draw() {
                super.draw();
                if (selected.get() != null) {
                    selected.get().draw(pos.add(size.multiply(.5)));
                }
            }
        };

        UIShowOne health = new UIShowOne();
        UIShowOne color = new UIShowOne();
        selected.forEach(d -> {
            if (d instanceof Creature) {
                name.text = () -> "Creature";
            } else if (d != null) {
                name.text = () -> "Block";
            } else {
                name.text = () -> "Select an object";
            }

            health.showing = space(0);
            health.parts.clear();

            if (d != null) {
                UIValue red = new UIValue("Red", x -> x < 32, x -> x > 0);
                red.value.set(32);
                UIValue green = new UIValue("Green", x -> x < 32, x -> x > 0);
                green.value.set(32);
                UIValue blue = new UIValue("Blue", x -> x < 32, x -> x > 0);
                blue.value.set(32);
                UIList colors = list(false, red, green, blue);
                colors.gravity = 1;
                Supplier<Color4> getColor = () -> new Color4(red.value.get() / 32., green.value.get() / 32., blue.value.get() / 32.);
                Function<Supplier<Color4>, UIElement> colorButton = s -> {
                    UIElement button = space(30);
                    button.color = s;
                    button.border = true;
                    button.onClick.onEvent(() -> {
                        Color4 c = s.get();
                        red.value.set((int) Math.round(c.r * 32));
                        green.value.set((int) Math.round(c.g * 32));
                        blue.value.set((int) Math.round(c.b * 32));
                        //showColor.color = () -> c;
                    });
                    UIShowOne r = new UIShowOne();
                    r.add(button);
                    r.showing = button;
                    return r;
                };
                UIList chooseColor1 = list(true,
                        colorButton.apply(() -> RED),
                        colorButton.apply(() -> ORANGE),
                        colorButton.apply(() -> YELLOW),
                        colorButton.apply(() -> GREEN),
                        colorButton.apply(() -> BLUE),
                        colorButton.apply(() -> PURPLE));
                UIList chooseColor2 = list(true,
                        colorButton.apply(() -> WHITE),
                        colorButton.apply(() -> Color4.gray(.75)),
                        colorButton.apply(() -> Color4.gray(.5)),
                        colorButton.apply(() -> Color4.gray(.25)),
                        colorButton.apply(() -> BLACK),
                        colorButton.apply(Core.interval(.5).map(Color4::random)));
                chooseColor1.setAllPadding(new Vec2(10));
                chooseColor2.setAllPadding(new Vec2(10));

                red.value.combine(green.value, blue.value).onEvent(() -> {
                    d.color = getColor.get();
                });
                UIList colorList = list(false, chooseColor1, chooseColor2, colors);
                color.parts.add(colorList);
                color.showing = colorList;
                colorList.gravity = .5;
            } else {
                color.parts.clear();
                color.showing = space(0);
            }
        });
        selected.ofType(Creature.class).forEach(c -> {
            UIValue healthValue = new UIValue("Health", x -> x < ((Creature) selected.get()).maxHealth, x -> x > 0);
            healthValue.value.set(c.health);
            healthValue.value.forEach(x -> {
                c.health = x;
            });
            health.add(healthValue);
            health.showing = healthValue;
        });

        UIList ui = list(false, name, selectedView, health, color);
        ui.setAllPadding(new Vec2(10));
        ui.gravity = .5;

        Input.whenMouse(0, true).filter(() -> uiMode == 2).filter(notOverUI).onEvent(() -> {
            Tile.tileAt(Input.getMouse()).ifPresent(t -> selected.set(t.drawable));
        });

        return ui;
    }

    private static UIElement tileUI() {
        UIValue red = new UIValue("Red", x -> x < 32, x -> x > 0);
        red.value.set(32);
        UIValue green = new UIValue("Green", x -> x < 32, x -> x > 0);
        green.value.set(32);
        UIValue blue = new UIValue("Blue", x -> x < 32, x -> x > 0);
        blue.value.set(32);
        UIList colors = list(false, red, green, blue);
        colors.gravity = 1;
        Supplier<Color4> getColor = () -> new Color4(red.value.get() / 32., green.value.get() / 32., blue.value.get() / 32.);

        UISelector typeSelector = new UISelector("Tile", Arrays.asList("Tile", "Block", "Creature", "Clear"), () -> true);
        typeSelector.padding = new Vec2(20);
        UIElement showColor = new UIElement(new Vec2(200, 50));
        showColor.color = getColor;
        showColor.border = true;

        Function<Supplier<Color4>, UIElement> colorButton = s -> {
            UIElement button = space(30);
            button.color = s;
            button.border = true;
            button.onClick.onEvent(() -> {
                Color4 c = s.get();
                red.value.set((int) Math.round(c.r * 32));
                green.value.set((int) Math.round(c.g * 32));
                blue.value.set((int) Math.round(c.b * 32));
                //showColor.color = () -> c;
            });
            UIShowOne r = new UIShowOne();
            r.add(button);
            r.showing = button;
            return r;
        };
        UIList chooseColor1 = list(true,
                colorButton.apply(() -> RED),
                colorButton.apply(() -> ORANGE),
                colorButton.apply(() -> YELLOW),
                colorButton.apply(() -> GREEN),
                colorButton.apply(() -> BLUE),
                colorButton.apply(() -> PURPLE));
        UIList chooseColor2 = list(true,
                colorButton.apply(() -> WHITE),
                colorButton.apply(() -> Color4.gray(.75)),
                colorButton.apply(() -> Color4.gray(.5)),
                colorButton.apply(() -> Color4.gray(.25)),
                colorButton.apply(() -> BLACK),
                colorButton.apply(Core.interval(.5).map(Color4::random)));
        chooseColor1.setAllPadding(new Vec2(10));
        chooseColor2.setAllPadding(new Vec2(10));

        UIValue brushSize = new UIValue("Brush Size", x -> x < 5, x -> x > 1);
        brushSize.value.set(1);

        UIList ui = list(false, typeSelector, brushSize, space(15), showColor, chooseColor1, chooseColor2, colors);
        ui.gravity = .5;

        //Key Input
        Input.whenMouse(0, true).combineEventStreams(Input.whileMouseDown(1).limit(.05)).filter(() -> uiMode == 1)
                .filter(notOverUI).onEvent(() -> Tile.tilesNear(Input.getMouse(), brushSize.value.get()).forEach(t -> {
            Color4 color = getColor.get();
            switch (typeSelector.chosen.get()) {
                case "Tile":
                    t.color = color;
                    break;
                case "Block":
                    t.drawable = new Drawable(color, "box", t);
                    break;
                case "Creature":
                    t.drawable = new Creature(color, t);
                    break;
                case "Clear":
                    t.drawable = null;
                    break;
            }
        }));
        Input.whenMouse(2, true).filter(() -> uiMode == 1).filter(notOverUI).onEvent(() -> Tile.tileAt(Input.getMouse()).ifPresent(t -> {
            Color4 c = t.color;
            red.value.set((int) Math.round(c.r * 32));
            green.value.set((int) Math.round(c.g * 32));
            blue.value.set((int) Math.round(c.b * 32));
        }));
//        Input.whileKeyDown(Keyboard.KEY_B).filter(() -> uiMode == 1)
//                .filter(notOverUI).onEvent(() -> Tile.tilesNear(Input.getMouse(), 3).forEach(t -> {
//            Color4 color = getColor.get();
//            switch (typeSelector.chosen.get()) {
//                case "Tile":
//                    t.color = color;
//                    sendToAll(10, t.x, t.y, color);
//                    break;
//                case "Block":
//                    t.drawable = new Drawable(color, "box", t);
//                    sendToAll(11, t.x, t.y, color);
//                    break;
//                case "Creature":
//                    t.drawable = new Creature(color, t);
//                    sendToAll(12, t.x, t.y, color);
//                    break;
//                case "Clear":
//                    t.drawable = null;
//                    sendToAll(13, t.x, t.y);
//                    break;
//            }
//        }));
        return ui;
    }
}
