/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guis;

import static gui.GUIController.FONT;
import gui.components.GUIButton;
import gui.components.GUIPanel;
import gui.types.ComponentInputGUI;
import gui.types.GUIInputComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import org.newdawn.slick.Color;
import util.Color4;
import util.Vec2;

/**
 *
 * @author Cruz
 */
public class Options extends ComponentInputGUI {

    private int perPage;
    private int index;
    private GUIButton back;

    private Vec2 bPos;
    private Vec2 bDim;

    private final Map<String, Object> settings = new HashMap();
    private final Map<String, Character> type = new HashMap();
    private static final Map<Class, UnaryOperator> setOpt;

    static{
        
        setOpt = new HashMap();
        
        setOpt.put(Boolean.class, (b) -> {
            
            return (Boolean) b == false;
        });
        
        setOpt.put(String.class, (s) -> s);
        
        setOpt.put(Integer.class, (i) -> {
        
            return null; //to have a gui...
        });
    }
    
    public Options(String n) {

        super(n);
        bPos = new Vec2(300, 150);
        bDim = new Vec2(300, FONT.getHeight() * 2);
        perPage = 3;
        index = 0;

        for (int i = 0; i < 3; i++) {

            components.add(new GUIPanel("" + i + " Panel", bPos.add(new Vec2(0, bDim.y * i)), bDim, Color4.ORANGE.multiply(8.0 - .2 * i)));
            inputs.add(new GUIButton("" + i, this, bPos.add(new Vec2(0, bDim.y * i)), bDim, "null", Color.white));
        }
        
        
    }
    
    public void start(){
        
        
    }
    
    private void change(String on){
        
        
    }
    
    private void retrieve(){
        
        
    }
    
    private void configure(){
        
        
    }

    @Override
    public void recieve(String string, Object o) {

        
    }

    @Override
    public GUIInputComponent getDefaultComponent() {

        return null;
    }
}
