/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guis;

import engine.Core;
import gui.GUI;
import static gui.GUIController.FONT;
import gui.components.GUILabel;
import gui.components.GUIPanel;
import gui.types.GUIComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.newdawn.slick.Color;
import util.Color4;
import util.Vec2;

/**
 *
 * @author Cruz
 */
public class Score extends GUI {

    private Map<String, Integer> scores = new HashMap();
    private List<GUIComponent> labels = new ArrayList();

    public Score(String n) {

        super(n);
        
        components.add(new GUIPanel("score panel", new Vec2(Core.screenWidth - 200, 700 - FONT.getHeight() * 11), new Vec2(200, FONT.getHeight() * 11), Color4.gray(.3).withA(.5)));
        components.add(new GUILabel("score label", new Vec2(Core.screenWidth - 200, 700 - FONT.getHeight()), "Scoreboard", Color.white));
        
        for (int i = 0; i < 10; i++) {
            
            labels.add(new GUILabel("" + i,new Vec2(Core.screenWidth - 200, 700 - FONT.getHeight() * (i + 2)), "----", Color.white));
        }
    }

    public void point(String n) {

        Integer sco = scores.get(n);

        if (sco != null) {
            
            scores.replace(n, sco + 1);
        }else{
            
            scores.put(n, 1);
        }
        
        reLabel();
    }
    
    private void reLabel(){
        
        List<String> clt = new ArrayList(scores.keySet());
        
        for (int i = 0; i < 10 && i < clt.size(); i++) {
            
            ((GUILabel) labels.get(i)).setLabel(clt.get(i) + ": " + scores.get(clt.get(i)));
        }
    }
    
    @Override
    public void draw(){
        
        super.draw();
        labels.forEach(GUIComponent::draw);
    }
}
