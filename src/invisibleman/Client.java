package invisibleman;

import commands.CommController;
import commands.Command;
import engine.Core;
import engine.Destructible;
import engine.Input;
import engine.Signal;
import graphics.data.Framebuffer;
import graphics.data.Framebuffer.DepthAttachment;
import graphics.data.Framebuffer.TextureAttachment;
import graphics.data.PostProcessEffect;
import graphics.data.Shader;
import gui.GUIController;
import gui.TypingManager;
import guis.Chat;
import guis.TitleScreen;
import static invisibleman.MessageType.*;
import java.util.Arrays;
import java.util.function.Consumer;
import map.CubeMap;
import network.Connection;
import network.NetworkUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL11.*;
import util.*;
import static util.Color4.TRANSPARENT;

public abstract class Client {

    public static boolean IS_MULTIPLAYER = false;
    private static Connection conn;
    public static TypingManager tpm;

    public static void main(String[] args) {
        if (IS_MULTIPLAYER) {
            //Try to connect to the server
            if (args.length == 0) {
                conn = NetworkUtils.connectManual();
            } else {
                conn = NetworkUtils.connect(args[0]);
            }

            //Handle messages recieved from the connection
            registerMessageHandlers();
        }

        //Set the game to 3D - this must go before Core.init();
        Core.is3D = true;
        Core.init();

        //Show the fps
        Core.render.bufferCount(Core.interval(1)).forEach(i -> Display.setTitle("FPS: " + i));

        TitleScreen ts = new TitleScreen("main menu", new Vec2(Core.screenWidth, Core.screenHeight));
        tpm = new TypingManager(ts);
        GUIController.add(ts);

        //Sounds.playSound("ethereal.mp3", true, .05);
        Core.update.onEvent(GUIController::update);
        Core.renderLayer(100).onEvent(GUIController::draw);

        //Start the game        
        ts.startGame();
        Core.run();

        //Force the program to stop
        System.exit(0);
    }

    public static void game() {

        //Hide the mouse
        Mouse.setGrabbed(true);

        //Load the level
        CubeMap.load("levels/level3.txt");

        //Setup graphics effects
        setupGraphics();

        //Set up GUI
        Chat con = new Chat("Con1", Keyboard.KEY_T, new Vec2(700, 700));
        GUIController.add(con);
        con.addChat("Welcome to a new game of Invisible Man!! Try to use left click to"
                + " shoot a snowball and 't' to access chat. If there are any bugs,"
                + " report to Rory Soiffer or become a traitor!!!");
        
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

        //Create the player
        new InvisibleMan().create();
    }

    public static void handleMessage(MessageType type, Consumer<Object[]> handler) {
        conn.registerHandler(type.id(), () -> {
            Object[] data = new Object[type.dataTypes.length];
            for (int i = 0; i < type.dataTypes.length; i++) {
                data[i] = conn.read(type.dataTypes[i]);
            }
            ThreadManager.onMainThread(() -> handler.accept(data));
        });
    }

    public static void registerMessageHandlers() {
        handleMessage(FOOTSTEP, a -> {
            Footstep f = new Footstep();
            f.create();
            f.set((Vec3) a[0], (double) a[1], (boolean) a[2], (double) a[3]);
        });
        handleMessage(SNOWBALL, a -> {
            BallAttack b = new BallAttack();
            b.create();
            b.get("position", Vec3.class).set((Vec3) a[0]);
            b.get("velocity", Vec3.class).set((Vec3) a[1]);
            b.isEnemy = true;
        });
        handleMessage(HIT, a -> {
            new Explosion((Vec3) a[0], new Color4(1, 0, 0)).create();
            Sounds.playSound("hit.wav");
        });
        handleMessage(SMOKE, a -> {
            Smoke f = new Smoke();
            f.create();
            f.get("position", Vec3.class).set((Vec3) a[0]);
            f.get("opacity", Double.class).set((double) a[1]);
        });
        handleMessage(RESTART, a -> {
            RegisteredEntity.getAll(BallAttack.class, Explosion.class, Footstep.class, Smoke.class, InvisibleMan.class).forEach(Destructible::destroy);
            new InvisibleMan().create();
        });
    }

    public static void sendMessage(MessageType type, Object... contents) {
        if (conn != null && !conn.isClosed()) {
            if (!type.verify(contents)) {
                throw new RuntimeException("Data " + Arrays.toString(contents) + " does not fit message type " + type);
            }
            conn.sendMessage(type.id(), contents);
        }
    }

    public static void setupGraphics() {
        //Silly graphics effects
        Signal<Integer> cMod = Input.whenMouse(1, true).reduce(0, i -> (i + 1) % 5);
        new PostProcessEffect(10, new Framebuffer(new TextureAttachment(), new DepthAttachment()),
                new Shader("default.vert", "invert.frag")).toggleOn(cMod.map(i -> i == 1));
        new PostProcessEffect(10, new Framebuffer(new TextureAttachment(), new DepthAttachment()),
                new Shader("default.vert", "grayscale.frag")).toggleOn(cMod.map(i -> i == 2));
        Shader wobble = new Shader("default.vert", "wobble.frag");
        Core.time().forEach(t -> wobble.setVec2("wobble", new Vec2(.01 * Math.sin(3 * t), .01 * Math.cos(3.1 * t)).toFloatBuffer()));
        new PostProcessEffect(10, new Framebuffer(new TextureAttachment(), new DepthAttachment()),
                wobble).toggleOn(cMod.map(i -> i == 3));
        new PostProcessEffect(10, new Framebuffer(new TextureAttachment(), new DepthAttachment()),
                new Shader("default.vert", "gamma.frag")).toggleOn(cMod.map(i -> i == 4));

        //Create the snow particles
        //new Snow().create();
        //Create the fog
        new Fog(new Color4(.95, .8, .3), .0025, .95).create();

        //Draw the level
        Core.render.onEvent(() -> {
            CubeMap.drawAll();
        });
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
