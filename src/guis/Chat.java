/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guis;

import commands.CommController;
import engine.Input;
import static gui.GUIController.FONT;
import gui.components.GUICommandField;
import gui.components.GUIListOutputField;
import gui.components.GUIPanel;
import gui.types.ComponentInputGUI;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.Color;
import util.Color4;
import util.Vec2;
import gui.types.GUIInputComponent;
import static gui.TypingManager.typing;

/**
 *
 * @author Cruz
 */
public class Chat extends ComponentInputGUI {

    private final GUIListOutputField output;
    private final GUICommandField input;
    private final Vec2 pos;
    private final Vec2 dim;

    public Chat(String n, int key, Vec2 p, Vec2 d) {

        super(n);
        pos = p;
        dim = d;
        components.add(new GUIPanel("Output Panel", pos, dim.subtract(new Vec2(0, FONT.getHeight())), Color4.gray(.3).withA(.5)));
        components.add(new GUIPanel("Input Panel", pos.add(new Vec2(0, dim.y - FONT.getHeight())), dim.withY(FONT.getHeight()), Color4.BLACK.withA(.5)));
        output = new GUIListOutputField("Output Field", this, pos.add(new Vec2(0, dim.y - FONT.getHeight())), dim.subtract(new Vec2(0, 2 * FONT.getHeight())), Color.white);
        input = new GUICommandField("Input Field", this, pos.add(new Vec2(0, dim.y)), dim.x, Color.white, Color4.WHITE);

        Input.whenKey(key, true).onEvent(() -> {

            this.setVisible(true);
            Mouse.setGrabbed(false);
            typing(this, true);
        });

        Input.whenKey(Keyboard.KEY_BACKSLASH, true).onEvent(() -> {

            this.setVisible(true);
            Mouse.setGrabbed(false);
            typing(this, true, "\\");
            input.setText("\\");
        });
    }

    @Override
    public GUIInputComponent getDefaultComponent() {

        for (GUIInputComponent gcf : inputs) {

            if (gcf.getName().equals("Input Field")) {

                return gcf;
            }
        }

        return null;
    }

    @Override
    public void recieve(String name, Object text) {

        String t = (String) text;

        if (!t.isEmpty() && t.charAt(0) == '\\') {

            output.appendLine(CommController.runCommand(t));
        } else {

            output.appendLine(t);
        }

        inputs.forEach(gcf -> {

            if (gcf instanceof GUICommandField) {

                input.resetIndex();
            }
        });
    }

    @Override
    public void update() {

        super.update();
        output.update();
        input.update();
    }

    @Override
    public void draw() {

        super.draw();
        output.draw();
        inputs.forEach(i -> i.draw());
    }

    public void addChat(String s) {

        output.appendLine(s);
    }
}
