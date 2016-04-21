/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guis;

import gui.types.ComponentInputGUI;
import gui.types.GUIInputComponent;

/**
 *
 * @author Cruz
 */
public class ChangeInt extends ComponentInputGUI{

    public ChangeInt(String n) {
        
        super(n);
    }

    @Override
    public void recieve(String string, Object o) {

        
    }

    @Override
    public GUIInputComponent getDefaultComponent() {

        return null;
    }
}
