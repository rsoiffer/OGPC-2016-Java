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
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.lwjgl.input.Mouse;
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

    private GUIButton next;
    private GUIButton prev;
    private GUIButton back;

    private Vec2 bPos;
    private Vec2 bDim;

    private final Map<String, Object> settings = new HashMap();
    private final Map<String, int[]> smm = new HashMap();

    public Options(String n) {

        super(n);
        bPos = new Vec2(300, 150);
        bDim = new Vec2(300, FONT.getHeight() * 2);
        perPage = 3;
        index = 0;

        next = new GUIButton("prev", this, bPos.subtract(new Vec2(0, bDim.y)), bDim.divide(new Vec2(2, 1)), "Prev", Color.orange);
        prev = new GUIButton("next", this, bPos.subtract(new Vec2(0, bDim.y)).add(new Vec2(bDim.x / 2.0, 0)), bDim.divide(new Vec2(2, 1)), "Next", Color.orange);
        back = new GUIButton("back", this, bPos.subtract(new Vec2(0, bDim.y * 2)), bDim, "Back", Color.orange);

        components.add(new GUIPanel("next panel", bPos.subtract(new Vec2(0, bDim.y)), bDim.divide(new Vec2(2, 1)), Color4.BLUE.multiply(.80)));
        components.add(new GUIPanel("prev panel", bPos.subtract(new Vec2(0, bDim.y)).add(new Vec2(bDim.x / 2.0, 0)), bDim.divide(new Vec2(2, 1)), Color4.BLUE.multiply(.80)));
        components.add(new GUIPanel("back panel", bPos.subtract(new Vec2(0, bDim.y * 2)), bDim, Color4.BLUE.multiply(.50)));

        for (int i = 0; i < 3; i++) {

            components.add(new GUIPanel("" + i + " panel", bPos.add(new Vec2(0, bDim.y * i)), bDim, Color4.ORANGE.multiply(.8 - (.2 * i))));
            inputs.add(new GUIButton("" + i, this, bPos.add(new Vec2(0, bDim.y * i)), bDim, "", Color.white));
        }

        getOptions();
    }

    private void newInt(String on, int pv, int ma, int mi) {

        ChangeInt ci = ((ChangeInt) GUIController.getGUI("chai"));
        ci.setMM(mi, ma);
        ci.start(pv, this, on);
    }

    public void start() {

        this.setVisible(true);
        grabbed = Mouse.isGrabbed();
        Mouse.setGrabbed(false);
        typing(this, true);
    }

    private void decide(String name) {

        Object vl = settings.get(next);
        
        if(vl instanceof Boolean){
            
            settings.replace(name, !((Boolean) vl));
        }else if(vl instanceof Integer){
            
            int[] mm = smm.get(name);
            newInt(name, (Integer) vl, mm[1], mm[0]);
        }
    }

    public void changeInt(String on, int v) {

        settings.replace(on, v);
    }

    public Object getValue(String optN) {

        return settings.get(optN);
    }

    private void setButtons() {

        List<String> ops = new ArrayList(settings.keySet());
        for (int i = 0; i < inputs.size() && i < 3 * index && i < ops.size(); i++) {

            ((GUIButton) inputs.get(i)).setLabel(ops.get(i));
        }
    }

    @Override
    public void recieve(String name, Object o) {

        System.out.println(name);
        
        if (name.length() == 1) {

            GUIButton gb = getButton((String) (new ArrayList(settings.keySet())).get(Integer.parseInt(name) * index));
            System.out.println(gb);
            
            if (gb != null) {

                String oName = gb.getLabel();
                decide(oName);
            }

        } else if (name.equals("prev")) {

            prev();
        } else if (name.equals("next")) {

            next();
        } else if (name.equals("back")) {

            this.setVisible(false);
            Mouse.setGrabbed(grabbed);
            typing(this, false);
            ((TitleScreen) GUIController.getGUI("main menu")).start();
        }
    }

    private void next() {

        index++;

        if (index * 3 >= settings.size()) {

            index = settings.size() / 3;
        }

        setButtons();
    }

    private void prev() {

        index--;

        if (index < 0) {

            index = 0;
        }

        setButtons();
    }

    private GUIButton getButton(String n) {

        for (GUIInputComponent gb : inputs) {

            if (gb.getName().equals(n)) {

                return (GUIButton) gb;
            }
        }

        return null;
    }

    private void getOptions() {

        try {

            Scanner reader = new Scanner(new File("options.opts"));

            while (reader.hasNext()) {

                read(reader.nextLine());
            }
            
            reader.close();
        } catch (FileNotFoundException ex) {

            System.out.println("error while loading the options folder :(");
        }
    }

    private void read(String ln) {

        System.out.println("");
        
        int ind = ln.indexOf(" ");
        String name = ln.substring(0, ind++);

        char type = ln.charAt(ind);
        ln = ln.substring(ind += 2);

        switch (type) {

            case 'b':
                boolean b = Boolean.parseBoolean(ln);
                settings.put(name, b);
                break;
            case 's':
                String s = ln;
                settings.put(name, s);
                break;
            case 'i':
                ind = ln.indexOf(" ");
                int[] mm = new int[2];
                mm[0] = Integer.parseInt(ln.substring(0, ind++));
                int ind2 = ln.indexOf(" ", ind);
                mm[1] = Integer.parseInt(ln.substring(ind, ind2++));
                int vl = Integer.parseInt(ln.substring(ind2));
                settings.put(name, vl);
                smm.put(name, mm);
                break;
        }
    }

    @Override
    public GUIInputComponent getDefaultComponent() {

        return null;
    }

    @Override
    public List<GUIComponent> mousePressed(Vec2 p) {
        
        List<GUIComponent> logc = super.mousePressed(p);
        
        if(prev.containsClick(p)){
            
            logc.add(prev);
        }
        
        if(next.containsClick(p)){
            
            logc.add(next);
        }
        
        if(back.containsClick(p)){
            
            logc.add(back);
        }
        
        return logc;
    }
    
    @Override
    public void draw(){
        
        super.draw();
        
        next.draw();
        prev.draw();
        back.draw();
    }
}
