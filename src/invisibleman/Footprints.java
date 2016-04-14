/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package invisibleman;

/**
 *
 * @author Cruz
 */
public enum Footprints {
    
    PAW("footstep_paw"),
    SHOE("footstep_white"),
    STEVE("footstep_steve");
    
    private String texDir;
    
    private Footprints(String td){
        
        texDir = td;
    }
    
    public String getDir(){
        
        return texDir;
    }
}
