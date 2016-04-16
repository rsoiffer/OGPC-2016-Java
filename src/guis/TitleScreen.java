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
import invisibleman.Client;
import java.util.ArrayList;
import java.util.List;
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
    private final Vec2 bMid;

    public TitleScreen(String n, Vec2 d) {

        super(n);
        dim = d;
        bDim = new Vec2(100, FONT.getHeight());
        bMid = d.divide(2).subtract(bDim.divide(2));

        components.add(new GUIPanel("back", Vec2.ZERO, dim, Color4.BLACK));
        components.add(new GUIPanel("options Panel", bMid, bDim, Color4.RED));
        components.add(new GUIPanel("servers Panel", bMid.add(new Vec2(0, bDim.y)), bDim, Color4.ORANGE.multiply(0.75)));
        components.add(new GUIPanel("play Panel", bMid.add(new Vec2(0, bDim.y * 2)), bDim, Color4.ORANGE.multiply(0.5)));

        buttons.add(new GUIButton("options", this, bMid, bDim, "Options", black));
        buttons.add(new GUIButton("servers", this, bMid.add(new Vec2(0, bDim.y)), bDim, "Servers", black));
        buttons.add(new GUIButton("play", this, bMid.add(new Vec2(0, bDim.y * 2)), bDim, "Play", black));
    }

    @Override
    public void draw() {

        super.draw();
        buttons.forEach(GUIButton::draw);
    }

    public void startGame() {

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
            Client.game();
        }
    }

    @Override
    public GUIInputComponent getDefaultComponent() {

        return null;
    }

}
