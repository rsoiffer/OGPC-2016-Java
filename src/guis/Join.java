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
import gui.components.GUICommandField;
import gui.components.GUIPanel;
import gui.types.ComponentInputGUI;
import gui.types.GUIInputComponent;
import invisibleman.Client;
import invisibleman.Game;
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
public class Join extends ComponentInputGUI {
    
    private Vec2 fPos;
    private Vec2 fDim;
    
    private boolean editor;
    private boolean grabbed;
    private boolean server;
    
    public Join(String n) {
        
        super(n);
        fPos = new Vec2(600, 150);
        fDim = new Vec2(300, FONT.getHeight() * 2);
        
        components.add(new GUIPanel("ip Panel", fPos, fDim, Color4.ORANGE.multiply(.6)));
        components.add(new GUIPanel("back Panel", fPos.subtract(new Vec2(0, fDim.y)), fDim, Color4.BLUE.multiply(1.)));
        inputs.add(new GUICommandField("ip Input", this, fPos.add(new Vec2(0, FONT.getHeight() * 1.5)), fDim.x, Color.white, Color4.WHITE));
        inputs.add(new GUIButton("back play", this, fPos.subtract(new Vec2(0, fDim.y)), fDim, "Back", Color.orange));
    }
    
    @Override
    public void recieve(String name, Object info) {
        
        if (name.equals("ip Input")) {
            
            TitleScreen.setMainVisibleFalse();
            Mouse.setGrabbed(true);
            typing(this, false);
            
            
            if (editor) {
                
                Client.IS_MULTIPLAYER = true;
                if(server){
                    
                    Editor.start("current", (String) info);
                }else{
                    
                    Client.IS_MULTIPLAYER = false;
                    Client.fogColor = Color4.WHITE;
                    CubeMap.save("levels/level_" + (String) info + ".txt");
                    Editor.start((String) info, "");
                }
            } else {
                
                Game.start("current", (String) info);
            }
        } else if (name.equals("back play")) {
            
            this.setVisible(false);
            Mouse.setGrabbed(true);
            typing(this, false);
            ((Play) GUIController.getGUI("level select")).start(editor ? 1 : 0);
        }
    }
    
    @Override
    public GUIInputComponent getDefaultComponent() {
        
        return inputs.get(0);
    }
    
    public void start(int mode) {
        editor = mode == 1  || mode == 2;
        server = mode == 1;
        this.setVisible(true);
        grabbed = Mouse.isGrabbed();
        Mouse.setGrabbed(false);
        typing(this, true);
    }
}
