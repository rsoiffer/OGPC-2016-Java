/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guis;

import static gui.GUIController.FONT;
import static gui.TypingManager.typing;
import gui.components.GUIButton;
import gui.components.GUILabel;
import gui.components.GUIPanel;
import gui.types.ComponentInputGUI;
import gui.types.GUIInputComponent;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.Color;
import util.Color4;
import util.Vec2;

/**
 *
 * @author Cruz
 */
public class ChangeInt extends ComponentInputGUI {

    private final Vec2 bPos;
    private final Vec2 bDim;

    private int val;
    private int max;
    private int min;

    private Options from;
    private String opName;

    public ChangeInt(String n) {

        super(n);
        bPos = new Vec2(600, 150);
        bDim = new Vec2(300, FONT.getHeight() * 2);

        inputs.add(new GUIButton("down", this, bPos, bDim, "Subtract", Color.white));
        inputs.add(new GUIButton("up", this, bPos.add(new Vec2(0, bDim.y * 2)), bDim, "Add", Color.white));
        inputs.add(new GUIButton("set", this, bPos.add(new Vec2(0, bDim.y * -1)), bDim, "Set", Color.orange));

        components.add(new GUILabel("val", bPos.add(new Vec2(0, bDim.y)), bDim, "0", Color.white));
        components.add(new GUIPanel("set panel", bPos.add(new Vec2(0, bDim.y * -1)), bDim, Color4.BLUE.multiply(1.)));

        for (int i = 0; i < 3; i++) {

            components.add(new GUIPanel("opts " + i, bPos.add(new Vec2(0, bDim.y * i)), bDim, Color4.ORANGE.multiply(.6 - (.2 * i))));
        }
    }

    @Override
    public void recieve(String name, Object o) {

        System.out.println(name);
        
        if (name.equals("down")) {

            val--;

            if (val < min) {

                val = min;
            }
        } else if (name.equals("up")) {

            val++;

            if (val > max) {

                val = max;
            }
        } else if (name.equals("back")){

            this.setVisible(false);
            from.changeInt(opName, val);
            Mouse.setGrabbed(grabbed);
            typing(from, true);
        }
    }

    public void start(int pre, Options op, String n) {

        val = pre;
        ((GUILabel) components.get(0)).setLabel("" + pre);
        from = op;
        opName = n;

        this.setVisible(true);
        grabbed = Mouse.isGrabbed();
        Mouse.setGrabbed(false);
        typing(this, true);
    }

    public void setMM(int mi, int ma) {

        max = ma;
        min = mi;
    }

    @Override
    public GUIInputComponent getDefaultComponent() {

        return null;
    }
}
