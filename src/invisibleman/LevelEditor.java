package invisibleman;

import engine.Core;
import engine.Input;
import engine.Signal;
import graphics.Window2D;
import java.io.PrintWriter;
import java.util.function.Supplier;
import org.lwjgl.input.Keyboard;
import ui.UIElement;
import static ui.UIElement.space;
import ui.UIList;
import static ui.UIList.list;
import ui.UIShowOne;
import ui.UIValue;
import util.Color4;
import util.Log;
import util.Util;
import util.Vec2;

public class LevelEditor {

    private static Supplier<Boolean> notOverUI;
    private static int uiMode;

    public static void main(String[] args) {
        //Game
        Core.init();

        Tile.TILE_SIZE = 50;
        Tile.HEIGHT_MULT = 0;

        double speed = 500;
        Input.whileKeyDown(Keyboard.KEY_W).forEach(dt -> Window2D.viewPos = Window2D.viewPos.add(new Vec2(0, speed * dt)));
        Input.whileKeyDown(Keyboard.KEY_A).forEach(dt -> Window2D.viewPos = Window2D.viewPos.add(new Vec2(speed * -dt, 0)));
        Input.whileKeyDown(Keyboard.KEY_S).forEach(dt -> Window2D.viewPos = Window2D.viewPos.add(new Vec2(0, speed * -dt)));
        Input.whileKeyDown(Keyboard.KEY_D).forEach(dt -> Window2D.viewPos = Window2D.viewPos.add(new Vec2(speed * dt, 0)));
        Core.render.onEvent(() -> Tile.all().forEach(Tile::draw2D));
        Input.mouseWheel.forEach(i -> Window2D.viewSize = Window2D.viewSize.multiply(Math.pow(.9, i / 120)));

        UI();

        Input.whenKey(Keyboard.KEY_O, true).onEvent(() -> {
            try {
                PrintWriter writer = new PrintWriter("level.txt", "UTF-8");
                Util.forRange(0, 50, 0, 50, (x, y) -> {
                    Tile t = Tile.grid[x][y];
                    writer.println(x + " " + y + " " + t.height + " " + (t.sprite != null ? t.sprite.name : ""));
                });
                writer.close();
            } catch (Exception ex) {
                Log.error(ex);
            }
        });
        Input.whenKey(Keyboard.KEY_L, true).onEvent(() -> Tile.load("level.txt"));

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
        UIElement raiseLowerUI = raiseLowerUI();
        screen.add(tileUI, raiseLowerUI);
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
                    screen.showing = raiseLowerUI;
                    break;
            }
        });
    }

    private static UIElement raiseLowerUI() {
        UIValue brushSize = new UIValue("Brush Size", x -> x < 30, x -> x > 1);
        brushSize.value.set(10);
        brushSize.padding = new Vec2(10);

        Input.whileMouseDown(0).filter(dt -> uiMode == 2).filter(dt -> notOverUI.get()).forEach(dt -> Tile.all().forEach(t -> {
            double dist2 = t.pos().toVec2().subtract(Input.getMouse()).lengthSquared();
            double strength = 15 * Math.pow(2, -dist2 / 1000 / brushSize.value.get() / brushSize.value.get());
            t.height = Math.min(32, t.height + dt * strength);
        }));
        Input.whileMouseDown(1).filter(dt -> uiMode == 2).filter(dt -> notOverUI.get()).forEach(dt -> Tile.all().forEach(t -> {
            double dist2 = t.pos().toVec2().subtract(Input.getMouse()).lengthSquared();
            double strength = 15 * Math.pow(2, -dist2 / 1000 / brushSize.value.get() / brushSize.value.get());
            t.height = Math.min(32, t.height - dt * strength);
        }));

        return brushSize;
    }

    private static UIElement tileUI() {
        UIValue height = new UIValue("Height", x -> x < 32, x -> x > -32);

        UIElement showColor = new UIElement(new Vec2(200, 50));
        showColor.color = height.value.map(v -> Color4.gray(v / 64. + .5));;
        showColor.border = true;

        UIValue brushSize = new UIValue("Brush Size", x -> x < 10, x -> x > 1);
        brushSize.value.set(1);

        UIList ui = list(false, space(25), showColor, space(10), height, brushSize);
        ui.gravity = .5;
        ui.setAllPadding(new Vec2(10));

        //Key Input
        Input.whenMouse(0, true).combineEventStreams(Input.whileMouseDown(1).limit(.05)).filter(() -> uiMode == 1)
                .filter(notOverUI).onEvent(() -> Tile.tilesNear(Input.getMouse(), brushSize.value.get()).forEach(t -> {
            t.height = height.value.get();
        }));
        Input.whenMouse(2, true).filter(() -> uiMode == 1).filter(notOverUI).onEvent(() -> Tile.tileAt(Input.getMouse()).ifPresent(t -> {
            height.value.set((int) Math.round(t.height));
        }));
        return ui;
    }
}
