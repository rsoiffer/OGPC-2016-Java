/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guis;

import gui.GUI;
import static gui.GUIController.FONT;
import gui.components.GUIListOutputField;
import gui.components.GUIPanel;
import gui.types.GUIComponent;
import org.newdawn.slick.Color;
import util.Color4;
import util.Vec2;

/**
 *
 * @author cbarnum18
 */
public class MiniChat extends GUI{
    
    GUIListOutputField out;
    
    public MiniChat(String n){
        
        super(n);
        
        out = new GUIListOutputField("chaty", this, new Vec2(0, 300 + FONT.getHeight()), new Vec2(300), Color.white);
        components.add(new GUIPanel("chat panel", Vec2.ZERO, new Vec2(300), Color4.gray(.3).withA(.5)));
    }
    
    public void append(String s){
        
        out.appendLine(s);
        update();
    }
    
    @Override
    public void draw(){
        
        components.forEach(GUIComponent::draw);
        out.draw();
    }
}
