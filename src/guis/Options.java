/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guis;

import gui.components.GUIButton;
import gui.types.ComponentInputGUI;
import gui.types.GUIInputComponent;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Cruz
 */
public class Options extends ComponentInputGUI{
    
    private int perPage;
    private GUIButton back;
    private Map<String, Object> settings = new HashMap();
    
    public Options(String n){
        
        super(n);
    }

    @Override
    public void recieve(String string, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GUIInputComponent getDefaultComponent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
