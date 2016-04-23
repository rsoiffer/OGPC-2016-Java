package game;

import commands.CommController;
import commands.Command;
import engine.Core;
import engine.Input;
import engine.Signal;
import graphics.data.Framebuffer;
import graphics.data.PostProcessEffect;
import graphics.data.Shader;
import gui.GUIController;
import guis.Chat;
import guis.Score;
import map.CubeMap;
import map.CubeType;
import networking.Client;
import static networking.Client.con;
import static networking.Client.sendMessage;
import static networking.MessageType.BLOCK_PLACE;
import static networking.MessageType.GET_NAME;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import static org.lwjgl.opengl.GL11.*;
import static util.Color4.BLACK;
import static util.Color4.TRANSPARENT;
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

    public static void setName(String n) {

        name = n;
    }

    public static void start(String map, String ip) {
        Client.connect(ip);

        //Hide the mouse
        Mouse.setGrabbed(true);

        //Setup graphics effects
        setupGraphics();

        //Load the level
        CubeMap.load("levels/level_" + map + ".txt");

        //Set up GUI
        setupGUI();

        //Create the player
        new InvisibleMan().create();
    }

    public static void setupGraphics() {
        //Silly graphics effects
        /*
        Signal<Integer> cMod = Input.whenMouse(1, true).reduce(0, i -> (i + 1) % 5);
        new PostProcessEffect(10, new Framebuffer(new Framebuffer.TextureAttachment(), new Framebuffer.DepthAttachment()),
                new Shader("default.vert", "invert.frag")).toggleOn(cMod.map(i -> i == 1));
        new PostProcessEffect(10, new Framebuffer(new Framebuffer.TextureAttachment(), new Framebuffer.DepthAttachment()),
                new Shader("default.vert", "grayscale.frag")).toggleOn(cMod.map(i -> i == 2));
        Shader wobble = new Shader("default.vert", "wobble.frag");
        Core.time().forEach(t -> wobble.setVec2("wobble", new Vec2(.01 * Math.sin(3 * t), .01 * Math.cos(3.1 * t)).toFloatBuffer()));
        new PostProcessEffect(10, new Framebuffer(new Framebuffer.TextureAttachment(), new Framebuffer.DepthAttachment()),
                wobble).toggleOn(cMod.map(i -> i == 3));
        new PostProcessEffect(10, new Framebuffer(new Framebuffer.TextureAttachment(), new Framebuffer.DepthAttachment()),
                new Shader("default.vert", "gamma.frag")).toggleOn(cMod.map(i -> i == 4));
        */

        //Create the fog
        new Fog(BLACK, .0025, .95).create(); // .95 .8 .3

        //Draw the level
        Core.render.onEvent(() -> {
            CubeMap.drawAll();
        });
    }

    private static void setupGUI() {
        con = new Chat("Con1", Keyboard.KEY_T, new Vec2(700, 700));
        Score sb = new Score("scorb");
        GUIController.add(con, sb);

        con.addChat("Welcome " + name + " to invisible man! To move around, just"
                + " use WASD. To use chat, press T. To change your name, use the"
                + " \\name command. Have fun!");

        sb.setVisible(true);

        CommController.add(new Command("\\name", al -> {

            if (al.size() != 1) {
                return "\\name needs to have something to change your name to.";
            }

            if (name.length() > 14) {
                return "Your name is too long. Maximum 8 characters.";
            }
            name = al.get(0);
            Client.sendMessage(GET_NAME, name);
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

    public static PostProcessEffect kawaseBloom() {
        Framebuffer base = new Framebuffer(new Framebuffer.HDRTextureAttachment(), new Framebuffer.DepthAttachment());
        Framebuffer hdr = new Framebuffer(new Framebuffer.HDRTextureAttachment(), new Framebuffer.DepthAttachment());
        Framebuffer blur = new Framebuffer(new Framebuffer.HDRTextureAttachment(), new Framebuffer.DepthAttachment());
        Shader kawase = new Shader("default.vert", "kawase.frag");
        Shader onlyHDR = new Shader("default.vert", "onlyHDR.frag");
        return new PostProcessEffect(5, base, () -> {
            hdr.clear(TRANSPARENT);
            hdr.with(() -> onlyHDR.with(base::render));
            kawase.with(() -> {
                kawase.setInt("size", 0);
                blur.clear(TRANSPARENT);
                blur.with(hdr::render);

                kawase.setInt("size", 1);
                hdr.clear(TRANSPARENT);
                hdr.with(blur::render);

                kawase.setInt("size", 2);
                blur.clear(TRANSPARENT);
                blur.with(hdr::render);

                kawase.setInt("size", 2);
                hdr.clear(TRANSPARENT);
                hdr.with(blur::render);

                kawase.setInt("size", 3);
                blur.clear(TRANSPARENT);
                blur.with(hdr::render);
            });
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            TRANSPARENT.glClearColor();
            base.render();
            glBlendFunc(GL_SRC_ALPHA, GL_ONE);
            blur.render();
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        });
    }
}
