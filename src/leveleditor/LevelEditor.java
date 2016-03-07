package leveleditor;

import engine.Core;
import engine.Input;
import engine.Signal;
import graphics.Window2D;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Supplier;
import org.lwjgl.input.Keyboard;
import ui.UIElement;
import static ui.UIElement.space;
import ui.UIList;
import static ui.UIList.list;
import ui.UIShowOne;
import ui.UIValue;
import util.Color4;
import util.Util;
import util.Vec2;

public class LevelEditor {

    private static Supplier<Boolean> notOverUI;
    private static int uiMode;

    public static void main(String[] args) {
        //Game
        Core.init();

        Tile.init();
        UI();

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
        UIElement creatureUI = raiseLowerUI();
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

    private static UIElement raiseLowerUI() {
        return null;
    }

    private static UIElement tileUI() {
        UIValue height = new UIValue("Height", x -> x < 32, x -> x > -32);
        //height.value.set(32);
        Supplier<Color4> getColor = height.value.map(v -> Color4.gray(v / 64. + .5));

        UIElement showColor = new UIElement(new Vec2(200, 50));
        showColor.color = getColor;
        showColor.border = true;

        UIValue brushSize = new UIValue("Brush Size", x -> x < 10, x -> x > 1);
        brushSize.value.set(1);

        UIList ui = list(false, space(25), showColor, space(10), height, brushSize);
        ui.gravity = .5;
        ui.setAllPadding(new Vec2(10));

        //Key Input
        Input.whenMouse(0, true).combineEventStreams(Input.whileMouseDown(1).limit(.05)).filter(() -> uiMode == 1)
                .filter(notOverUI).onEvent(() -> Tile.tilesNear(Input.getMouse(), brushSize.value.get()).forEach(t -> {
            t.color = getColor.get();
        }));
        Input.whenMouse(2, true).filter(() -> uiMode == 1).filter(notOverUI).onEvent(() -> Tile.tileAt(Input.getMouse()).ifPresent(t -> {
            Color4 c = t.color;
            height.value.set((int) Math.round(c.r * 64 - 32));
        }));
        return ui;
    }
}
