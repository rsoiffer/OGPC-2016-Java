/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guis;

import static gui.TypingManager.typing;
import gui.components.GUIButton;
import gui.components.GUIPanel;
import gui.types.ComponentInputGUI;
import gui.types.GUIComponent;
import gui.types.GUIInputComponent;
import invisibleman.Game;
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
    private final int bNum = 10;
    private int index;

    private GUIButton next;
    private GUIButton prev;

    private boolean grabbed;
    private boolean editor;

    public Play(String n, Vec2 d) {

        super(n);

        dim = d;
        bDim = new Vec2(300, 50);
        bPos = new Vec2(0, 175);
        index = 0;

        next = new GUIButton("next level", this, bPos.subtract(new Vec2(0, bDim.y)), bDim.divide(new Vec2(2, 1)), "Next", Color.orange);
        prev = new GUIButton("prev level", this, bPos.subtract(new Vec2(0, bDim.y)).add(new Vec2(bDim.x / 2.0, 0)), bDim.divide(new Vec2(2, 1)), "Prev", Color.orange);

        components.add(new GUIPanel("next panel", bPos.subtract(new Vec2(0, bDim.y)), bDim.divide(new Vec2(2, 1)), Color4.BLUE.multiply(.75)));
        components.add(new GUIPanel("prev panel", bPos.subtract(new Vec2(0, bDim.y)).add(new Vec2(bDim.x / 2.0, 0)), bDim.divide(new Vec2(2, 1)), Color4.BLUE.multiply(.50)));

        for (int i = 0; i < bNum; i++) {

            components.add(new GUIPanel("selec panel " + i, bPos.add(new Vec2(0, bDim.y * i)), bDim, Color4.ORANGE.multiply(1.0 - (.5 / bNum) * i)));
        }

        getLevels();
    }

    private void getLevels() {

        File[] levels = (new File("levels")).listFiles();

        for (int i = 0; i < levels.length; i++) {

            String label = levels[i].getName().substring(6);
            label = label.substring(0, label.indexOf("."));
            System.out.println(label);
            inputs.add(new GUIButton(label, this, bPos.add(new Vec2(0, bDim.y * (i % bNum))), bDim, label, Color.white));
        }
    }

    @Override
    public void recieve(String name, Object info) {

        switch (name) {
            case "next level":
                next();
                break;
            case "prev level":
                prev();
                break;
            default:
                this.setVisible(false);
                Mouse.setGrabbed(grabbed);
                typing(this, false);
                if (editor) {
                    Editor.start(name);
                } else {
                    Game.start(name);
                }
                break;
        }

    }

    public void start(int mode) {
        editor = mode == 1;
        this.setVisible(true);
        grabbed = Mouse.isGrabbed();
        Mouse.setGrabbed(false);
        typing(this, true);
    }

    private void next() {

        index++;

        if (index * bNum >= inputs.size()) {

            index = inputs.size() / bNum;
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

        components.forEach(GUIComponent::draw);

        for (int i = index * bNum; i < inputs.size() && i < (index + 1) * bNum; i++) {

            inputs.get(i).draw();
        }

        next.draw();
        prev.draw();
    }
}
