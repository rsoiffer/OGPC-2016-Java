/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guis;

import gui.GUIController;
import static gui.GUIController.FONT;
import static gui.TypingManager.typing;
import gui.components.GUIButton;
import gui.components.GUIPanel;
import gui.types.ComponentInputGUI;
import gui.types.GUIComponent;
import gui.types.GUIInputComponent;
import invisibleman.Client;
import invisibleman.Game;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import map.CubeMap;
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
    private GUIButton back;

    private boolean grabbed;
    private boolean editor;
    private Join multiP;

    public Play(String n, Vec2 d) {

        super(n);

        dim = d;
        bDim = new Vec2(300, FONT.getHeight() * 2);
        bPos = new Vec2(200, 150);
        index = 0;

        next = new GUIButton("next level", this, bPos.subtract(new Vec2(0, bDim.y)), bDim.divide(new Vec2(2, 1)), "Next", Color.orange);
        prev = new GUIButton("prev level", this, bPos.subtract(new Vec2(0, bDim.y)).add(new Vec2(bDim.x / 2.0, 0)), bDim.divide(new Vec2(2, 1)), "Prev", Color.orange);
        back = new GUIButton("back title", this, bPos.subtract(new Vec2(0, bDim.y * 2)), bDim, "Back", Color.orange);

        components.add(new GUIPanel("next panel", bPos.subtract(new Vec2(0, bDim.y)), bDim.divide(new Vec2(2, 1)), Color4.BLUE.multiply(.80)));
        components.add(new GUIPanel("prev panel", bPos.subtract(new Vec2(0, bDim.y)).add(new Vec2(bDim.x / 2.0, 0)), bDim.divide(new Vec2(2, 1)), Color4.BLUE.multiply(.70)));
        components.add(new GUIPanel("back panel", bPos.subtract(new Vec2(0, bDim.y * 2)), bDim, Color4.BLUE.multiply(.50)));

        for (int i = 0; i < bNum; i++) {

            components.add(new GUIPanel("selec panel " + i, bPos.add(new Vec2(0, bDim.y * i)), bDim, Color4.ORANGE.multiply(1.0 - (.5 / bNum) * i)));
        }
    }

    private void getLevels() {
        try {
            List<String> levels = Files.readAllLines(Paths.get("level_list.txt"));
            int j = 1;
            inputs.add(new GUIButton("join server", this, bPos, bDim, "Join A Server", Color.white));
            
            if(editor){
                
                inputs.add(new GUIButton("new level", this, bPos.add(new Vec2(0, bDim.y)), bDim, "Create a New Level", Color.white));
                j++;
            }
            
            for (int i = j; i < levels.size() + j; i++) {
                String label = levels.get(i - j);
                inputs.add(new GUIButton(label, this, bPos.add(new Vec2(0, bDim.y * (i % bNum))), bDim, label, Color.white));
            }
        } catch (IOException ex) {
        }
    }

    @Override
    public void recieve(String name, Object info) {

        switch (name) {
            case "new level":
                multiP.start(2);
                break;
            case "next level":
                next();
                break;
            case "prev level":
                prev();
                break;
            case "back title":
                this.setVisible(false);
                Mouse.setGrabbed(grabbed);
                typing(this, false);
                inputs.clear();
                ((TitleScreen) GUIController.getGUI("main menu")).start();
                break;
            case "join server":
                Mouse.setGrabbed(grabbed);
                typing(this, false);
                multiP.start(editor ? 1 : 0);
                break;
            default:
                TitleScreen.setMainVisibleFalse();
                Mouse.setGrabbed(grabbed);
                typing(this, false);
                Client.IS_MULTIPLAYER = false;
                if (editor) {
                    Editor.start(name, null);
                } else {
                    Game.start(name, null);
                }
                break;
        }

    }

    public void start(int mode) {
        editor = mode == 1;
        multiP = (Join) GUIController.getGUI("ip select");
        getLevels();
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
        
        if (back.containsClick(p)) {

            gcp.add(back);
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
        back.draw();
    }
}
