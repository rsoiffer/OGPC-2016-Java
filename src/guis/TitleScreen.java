/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guis;

import graphics.Window2D;
import gui.GUIController;
import static gui.GUIController.FONT;
import static gui.TypingManager.typing;
import gui.components.GUIButton;
import gui.components.GUIPanel;
import gui.types.ComponentInputGUI;
import gui.types.GUIComponent;
import gui.types.GUIInputComponent;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.Color;
import util.Color4;
import util.Vec2;

/**
 *
 * @author cbarnum18
 */
public class TitleScreen extends ComponentInputGUI {

    private final List<GUIButton> buttons = new ArrayList();
    private final Vec2 dim;
    private final Vec2 bDim;
    private final Vec2 bPos;

    public TitleScreen(String n, Vec2 d) {

        super(n);
        dim = d;
        bDim = new Vec2(300, FONT.getHeight() * 2);
        bPos = new Vec2(0, 150);

        Window2D.background = Color4.BLACK;
        components.add(new GUIPanel("options Panel", bPos, bDim, Color4.ORANGE));
        components.add(new GUIPanel("play Panel", bPos.add(new Vec2(0, bDim.y)), bDim, Color4.ORANGE.multiply(0.8)));
        components.add(new GUIPanel("editor Panel", bPos.add(new Vec2(0, bDim.y * 2)), bDim, Color4.ORANGE.multiply(0.6)));
        components.add(new GUIPanel("exit Panel", bPos.subtract(new Vec2(0, bDim.y)), bDim, Color4.BLUE.multiply(0.6)));

        buttons.add(new GUIButton("options", this, bPos, bDim, "Options", Color.white));
        buttons.add(new GUIButton("play", this, bPos.add(new Vec2(0, bDim.y)), bDim, "Play", Color.white));
        buttons.add(new GUIButton("editor", this, bPos.add(new Vec2(0, bDim.y * 2)), bDim, "Editor", Color.white));
        buttons.add(new GUIButton("exit", this, bPos.subtract(new Vec2(0, bDim.y)), bDim, "Exit", Color.orange));
    }

    public static void setMainVisibleFalse() {

        GUIController.getGUI("level select").setVisible(false);
        GUIController.getGUI("ip select").setVisible(false);
        GUIController.getGUI("main menu").setVisible(false);
    }

    @Override
    public void draw() {

        super.draw();
        buttons.forEach(GUIButton::draw);
    }

    public void start() {

        this.setVisible(true);
        grabbed = Mouse.isGrabbed();
        Mouse.setGrabbed(false);
        typing(this, true);
    }

    @Override
    public List<GUIComponent> mousePressed(Vec2 p) {

        List<GUIComponent> list = super.mousePressed(p);

        buttons.stream().filter((gb) -> (gb.containsClick(p))).forEach((gb) -> {

            list.add(gb);
        });

        return list;
    }

    @Override
    public void recieve(String name, Object o) {

        switch (name) {
            case "play":
                Mouse.setGrabbed(grabbed);
                typing(this, false);
                ((Play) GUIController.getGUI("level select")).start(0);
                break;
            case "editor":
                Mouse.setGrabbed(grabbed);
                typing(this, false);
                ((Play) GUIController.getGUI("level select")).start(1);
                break;
            case "exit":
                System.exit(1);
            case "options":
                Mouse.setGrabbed(grabbed);
                typing(this, false);
                ((Options) GUIController.getGUI("options Menu")).start();
                break;
            default:
                break;
        }

    }

    @Override
    public GUIInputComponent getDefaultComponent() {

        return null;
    }

}
