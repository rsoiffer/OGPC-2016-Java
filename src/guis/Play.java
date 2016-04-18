/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guis;

import graphics.Graphics2D;
import static gui.TypingManager.typing;
import gui.components.GUIButton;
import gui.types.ComponentInputGUI;
import gui.types.GUIComponent;
import gui.types.GUIInputComponent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import map.Editor;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.Color;
import util.Color4;
import util.Vec2;

/**
 *
 * @author Cruz
 */
public class Play extends ComponentInputGUI {

    private Vec2 dim;
    private Vec2 bDim;
    private Vec2 bPos;
    private final int bNum = 4;
    private int index;

    private GUIButton next;
    private GUIButton prev;
    private Vec2 cDim;
    
    private boolean grabbed;

    public Play(String n, Vec2 d) {

        super(n);

        dim = d;
        bDim = new Vec2(100, 25);
        bPos = new Vec2(100, 200);
        index = 0;

        cDim = new Vec2(75, 50);
        next = new GUIButton("next level", this, bPos.subtract(new Vec2(0, cDim.y * 2)), cDim, "Next", Color.black);
        prev = new GUIButton("prev level", this, bPos.subtract(new Vec2(0, cDim.y)), cDim, "Prev", Color.black);

        getLevels();
    }

    private void getLevels() {

        File[] levels = (new File("levels")).listFiles();

        for (int i = 0; i < levels.length; i++) {

            String label = levels[i].getName().substring(6);
            label = label.substring(0, label.indexOf("."));
            System.out.println(label);
            inputs.add(new GUIButton(label, this, bPos.add(new Vec2(0, dim.y * (i % bNum + 1))), bDim, label, Color.black));
        }
    }

    @Override
    public void recieve(String name, Object info) {

        if (name.equals("next level")) {
            
            next();
        }
        
        if (name.equals("prev level")) {
            
            prev();
        }
        
        System.out.println(name);
    }

    public void start() {

        this.setVisible(true);
        grabbed = Mouse.isGrabbed();
        Mouse.setGrabbed(false);
        typing(this, true);
    }

    private void next() {

        index++;

        if (index * bNum >= inputs.size()) {

            index = inputs.size() / bNum - 1;
        }
    }

    private void prev() {

        index--;

        if (index < 0) {

            index = 0;
        }
    }

    @Override
    public GUIInputComponent getDefaultComponent() {

        return null;
    }

    @Override
    public List<GUIComponent> mousePressed(Vec2 p) {

        List<GUIComponent> gcp = new ArrayList();

        for (int i = index * bNum; i < inputs.size() && i < (index + 1) * bNum; i++) {

            if (inputs.get(i).containsClick(p)) {

                gcp.add(inputs.get(i));
            }
        }

        if (next.containsClick(p)) {

            gcp.add(next);
        }

        if (prev.containsClick(p)) {

            gcp.add(prev);
        }

        return gcp;
    }

    @Override
    public void draw() {

        Graphics2D.fillRect(bPos.subtract(new Vec2(0, cDim.y)), cDim.multiply(new Vec2(0, 2)), Color4.BLUE);

        for (int i = index * bNum; i < inputs.size() && i < (index + 1) * bNum; i++) {

            inputs.get(i).draw();
            Graphics2D.fillRect(inputs.get(i).getPos(), inputs.get(i).getDim(), Color4.BLUE.multiply(1.0 - (1.0 / i)));
        }

        next.draw();
        prev.draw();
    }
}
