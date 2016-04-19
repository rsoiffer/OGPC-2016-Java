/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guis;

import static gui.GUIController.FONT;
import static gui.TypingManager.typing;
import gui.components.GUICommandField;
import gui.components.GUIPanel;
import gui.types.ComponentInputGUI;
import gui.types.GUIInputComponent;
import gui.types.GUITypingComponent;
import invisibleman.Client;
import invisibleman.Game;
import map.Editor;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.Color;
import util.Color4;
import util.Vec2;

/**
 *
 * @author Cruz
 */
public class Join extends ComponentInputGUI {

    private Vec2 fPos;
    private Vec2 fDim;
    private boolean editor;
    private boolean grabbed;

    public Join(String n) {

        super(n);
        fPos = new Vec2(300, 300);
        fDim = new Vec2(150, FONT.getHeight());

        components.add(new GUIPanel("ip Panel", fPos, fDim, Color4.BLUE));
        inputs.add(new GUICommandField("ip Input", this, fPos.add(new Vec2(0, FONT.getHeight())), fDim.x, Color.white, Color4.WHITE));
    }

    @Override
    public void recieve(String name, Object info) {

        if (name.equals("ip Input")) {

            this.setVisible(false);
            Mouse.setGrabbed(true);
            typing(this, false);

            Client.IS_MULTIPLAYER = true;
            if (editor) {
                Editor.start("current", (String) info);
            } else {
                Game.start("current", (String) info);
            }
        }
    }

    @Override
    public GUIInputComponent getDefaultComponent() {

        return inputs.get(0);
    }

    public void start(int mode) {
        editor = mode == 1;
        this.setVisible(true);
        grabbed = Mouse.isGrabbed();
        Mouse.setGrabbed(false);
        typing(this, true);
    }
}
