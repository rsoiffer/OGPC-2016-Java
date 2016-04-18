/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guis;

import static gui.GUIController.FONT;
import static gui.TypingManager.typing;
import gui.components.GUIButton;
import gui.components.GUIPanel;
import gui.types.ComponentInputGUI;
import gui.types.GUIComponent;
import gui.types.GUIInputComponent;
import invisibleman.Game;
import java.util.ArrayList;
import java.util.List;
import map.Editor;
import org.lwjgl.input.Mouse;
import static org.newdawn.slick.Color.black;
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
    private final Play lSel;

    public TitleScreen(String n, Vec2 d, Play ps) {

        super(n);
        dim = d;
        bDim = new Vec2(100, FONT.getHeight() * 2);
        bPos = new Vec2(0, 300);

        components.add(new GUIPanel("back", Vec2.ZERO, dim, Color4.BLACK));
        components.add(new GUIPanel("options Panel", bPos, bDim, Color4.ORANGE));
        components.add(new GUIPanel("servers Panel", bPos.add(new Vec2(0, bDim.y)), bDim, Color4.ORANGE.multiply(0.75)));
        components.add(new GUIPanel("play Panel", bPos.add(new Vec2(0, bDim.y * 2)), bDim, Color4.ORANGE.multiply(0.5)));
        components.add(new GUIPanel("editor Panel", bPos.add(new Vec2(0, bDim.y * 3)), bDim, Color4.ORANGE.multiply(0.25)));
                
        buttons.add(new GUIButton("options", this, bPos, bDim, "Options", black));
        buttons.add(new GUIButton("servers", this, bPos.add(new Vec2(0, bDim.y)), bDim, "Servers", black));
        buttons.add(new GUIButton("play", this, bPos.add(new Vec2(0, bDim.y * 2)), bDim, "Play", black));
        buttons.add(new GUIButton("editor", this, bPos.add(new Vec2(0, bDim.y * 3)), bDim, "Editor", black));
        
        lSel = ps;
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

        if (name.equals("play")) {

            this.setVisible(false);
            Mouse.setGrabbed(grabbed);
            typing(this, false);
            lSel.start();
//            Game.start();
        }
        if (name.equals("editor")) {
            this.setVisible(false);
            Mouse.setGrabbed(grabbed);
            typing(this, false);
            Editor.start();
        }
    }

    @Override
    public GUIInputComponent getDefaultComponent() {

        return null;
    }

}
