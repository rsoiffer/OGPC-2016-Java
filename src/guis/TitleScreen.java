/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guis;

import gui.components.GUIButton;
import gui.components.GUIPanel;
import gui.types.ComponentInputGUI;
import gui.types.GUIInputComponent;
import util.Color4;
import util.Vec2;

/**
 *
 * @author cbarnum18
 */
public class TitleScreen extends ComponentInputGUI{

    private final GUIButton options;
    private final GUIButton servers;
    private final GUIButton play;
    private final Vec2 dim;
    private final Vec2 bDim;
    private final Vec2 bMid;
    
    public TitleScreen(String n, Vec2 d) {
        
        super(n);
        dim = d;
        bDim = new Vec2(10, 10);
        bMid = new Vec2((d.x / 2) - bDim.x, (d.y / 2) - (bDim.y));
        
        components.add(new GUIPanel("back", Vec2.ZERO, dim, Color4.BLACK));
        components.add(new GUIPanel("options Panel", bMid, bMid.add(bDim), Color4.ORANGE));
        components.add(new GUIPanel("servers Panel", bMid.add(new Vec2(0, bDim.y)), bMid.add(bDim).add(new Vec2(0, bDim.y)), Color4.ORANGE.multiply(0.75)));
        components.add(new GUIPanel("play Panel", bMid.add(new Vec2(0, bDim.y * 2)), bMid.add(bDim).add(new Vec2(0, bDim.y * 2)), Color4.ORANGE.multiply(0.5)));
        
        options = null;
        servers = null;
        play = null;
        
        this.setVisible(true);
    }

    @Override
    public void recieve(String string, Object o) {
        
        
    }

    @Override
    public GUIInputComponent getDefaultComponent() {
        
        return null;
    }
    
}