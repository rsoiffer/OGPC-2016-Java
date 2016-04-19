package invisibleman;

import commands.CommController;
import commands.Command;
import gui.GUIController;
import guis.Chat;
import static invisibleman.Client.*;
import static invisibleman.MessageType.BLOCK_PLACE;
import map.CubeMap;
import map.CubeType;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import util.RegisteredEntity;
import util.Vec2;
import util.Vec3;

/**
 *
 * @author Grant
 */
public class Game {

    private static String name = "new player";

    public static String getName() {

        return name;
    }

    public static void start(String map) {
        Client.connect();

        //Hide the mouse
        Mouse.setGrabbed(true);

        //Load the level
        CubeMap.load("levels/level_" + map + ".txt");

        //Setup graphics effects
        setupGraphics();

        //Set up GUI
        setupGUI();

        //Create the player
        new InvisibleMan().create();
    }

    private static void setupGUI() {
        con = new Chat("Con1", Keyboard.KEY_T, new Vec2(700, 700));
        GUIController.add(con);
        con.addChat("Welcome " + name + " to invisible man! To move around, just"
                + " use WASD. To use chat, press T. To change your name, use the"
                + " \\name command. Have fun!");

        CommController.add(new Command("\\name", al -> {

            if (al.size() != 1) {
                return "\\name needs to have something to change your name to.";
            }

            name = al.get(0);
            return "Your name has been changed to " + name;
        }));

        CommController.add(new Command("\\step", al -> {

            if (al.size() != 1) {
                return "\\step only accepts one parameter.";
            }

            String print = al.get(0);

            try {

                FootstepType fst = FootstepType.valueOf(print.toUpperCase());
                Footstep.changePrint(fst.getDir());
                return "footsteps changed to " + print.toLowerCase();
            } catch (IllegalArgumentException iae) {

                return print + " is not a footprint type.";
            }
        }));

        CommController.add(new Command("\\clear", al -> {

            if (!al.isEmpty()) {
                return "\\clear does not accept any parameters.";
            }
            con.clearChat();

            return "";
        }));

        CommController.add(new Command("\\steplist", al -> {

            if (!al.isEmpty()) {
                return "\\steplist does not accept any parameters.";
            }
            String s = "";
            for (FootstepType fst : FootstepType.values()) {

                s += fst.name().toLowerCase() + " ";
            }
            return s;
        }));

        CommController.add(new Command("\\setblock", al -> {
            if (al.size() > 4) {
                return "Too many arguments. Usage: \\setblock (x) (y) (z) (type)";
            }
            if (al.size() < 4) {
                return "Not enough arguments. Usage: \\setblock (x) (y) (z) (type)";
            }
            int[] coords = new int[3];
            boolean[] relative = {false, false, false};
            Vec3 pos;
            Vec3 ppos = RegisteredEntity.get(InvisibleMan.class).get().get("position", Vec3.class).get();
            for (int i = 0; i < 3; i++) {
                if (al.get(i).startsWith(":")) {
                    relative[i] = true;
                    al.set(i, al.get(i).substring(1));
                }
                try {
                    coords[i] = Integer.parseInt(al.get(i));
                } catch (Exception e) {
                    return "Invalid argument: " + al.get(i) + ". Usage: \\setblock (x) (y) (z) (type)";
                }
            }
            pos = new Vec3(coords[0] + (relative[0] ? ppos.x : 0), coords[1] + (relative[1] ? ppos.y : 0), coords[2] + (relative[2] ? ppos.z : 0));

            try {
                CubeType ct = al.get(3).equals("null") ? null : CubeType.getByName(al.get(3));
                CubeMap.setCube((int) pos.x, (int) pos.y, (int) pos.z, ct);
                sendMessage(BLOCK_PLACE, pos, ct);
                return "Block placed.";
            } catch (Exception e) {
                return "Invalid block type.";
            }
        }));
        CommController.add(new Command("\\pos", al -> {
            if (!al.isEmpty()) {
                return "\\pos does not accept any parameters.";
            }
            Vec3 pos = RegisteredEntity.get(InvisibleMan.class).get().get("position", Vec3.class).get();
            return String.format("You are currently at: %f, %f, %f", pos.x, pos.y, pos.z);
        }));
        CommController.add(new Command("\\tp", al -> {
            if (al.size() != 3) {
                return "Usage: \tp (x) (y) (z)";
            }
            boolean[] relative = {false, false, false};
            int[] coords = new int[3];
            Vec3 pos;
            Vec3 ppos = RegisteredEntity.get(InvisibleMan.class).get().get("position", Vec3.class).get();
            for (int i = 0; i < 3; i++) {
                if (al.get(i).startsWith("~")) {
                    relative[i] = true;
                    al.set(i, al.get(i).substring(1));
                }
                try {
                    coords[i] = Integer.parseInt(al.get(i));
                } catch (Exception e) {
                    return "Invalid argument: " + al.get(i) + ". Usage: \\setblock (x) (y) (z) (type)";
                }
            }
            pos = new Vec3(coords[0] + (relative[0] ? ppos.x : 0), coords[1] + (relative[1] ? ppos.y : 0), coords[2] + (relative[2] ? ppos.z : 0));
            RegisteredEntity.get(InvisibleMan.class).get().get("position", Vec3.class).set(pos);
            return String.format("Teleported to %f %f %f.", pos.x, pos.y, pos.z);
        }));
    }
}
