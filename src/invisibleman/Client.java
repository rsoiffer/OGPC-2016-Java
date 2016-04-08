package invisibleman;

import engine.Core;
import engine.Destructible;
import engine.Input;
import engine.Signal;
import graphics.data.Framebuffer;
import graphics.data.Framebuffer.DepthAttachment;
import graphics.data.Framebuffer.TextureAttachment;
import graphics.data.PostProcessEffect;
import graphics.data.Shader;
import gui.Console;
import gui.GUIController;
import gui.TypingManager;
import map.CubeMap;
import network.Connection;
import network.NetworkUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL11.*;
import static util.Color4.TRANSPARENT;
import util.*;

public abstract class Client {

    public static boolean IS_MULTIPLAYER = false;
    public static Connection conn;

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
        //Hide the mouse
        Mouse.setGrabbed(true);
        //The reset button
        Input.whenKey(Keyboard.KEY_BACKSLASH, true).onEvent(() -> sendMessage(5));

        //Load the level
        CubeMap.load("level3.txt");

        //Setup graphics effects
        setupGraphics();

        //Create the trees
//        for (int i = -15; i <= 15; i += 1) {
//            for (int j = -15; j <= 15; j += 1) {
//
//                Tree t = new Tree();
//                t.create();
//                double x = Math.random() * 5 - 2.5;
//                double y = Math.random() * 5 - 2.5;
//                t.get("position", Vec3.class).set(new Vec3(i * 3 + x, j * 3 + y, 10));
//            }
//        }
        //Set up GUI
        Console console = new Console("Con1", Vec2.ZERO, new Vec2(1200, 300));
        GUIController.add(console);
        TypingManager.init("Con1", Keyboard.KEY_T);

        Core.update.onEvent(() -> {
            GUIController.update();
        });
        Core.renderLayer(100).onEvent(() -> {
            GUIController.draw();
        });

        //Create the player
        new InvisibleMan().create();

        //Start the game
        Core.run();

        //Force the program to stop
        System.exit(0);
    }

    public static void registerMessageHandlers() {
        conn.registerHandler(0, () -> {
            Vec3 pos = conn.read(Vec3.class);
            double rot = conn.read(Double.class);
            boolean isLeft = conn.read(Boolean.class);
            ThreadManager.onMainThread(() -> {
                Footstep f = new Footstep();
                f.create();
                f.set(pos, rot, isLeft);
            });
        });
        conn.registerHandler(1, () -> {
            Vec3 pos = conn.read(Vec3.class);
            Vec3 vel = conn.read(Vec3.class);
            ThreadManager.onMainThread(() -> {
                BallAttack b = new BallAttack();
                b.create();
                b.get("position", Vec3.class).set(pos);
                b.get("velocity", Vec3.class).set(vel);
                b.isEnemy = true;
            });
        });
        conn.registerHandler(2, () -> {
            Vec3 pos = conn.read(Vec3.class);
            ThreadManager.onMainThread(() -> {
                new Explosion(pos, new Color4(1, 0, 0)).create();
                Sounds.playSound("hit.wav");
            });
        });
        conn.registerHandler(5, () -> ThreadManager.onMainThread(() -> {
            RegisteredEntity.getAll(BallAttack.class, Explosion.class,
                    Footstep.class, InvisibleMan.class).forEach(Destructible::destroy);
            new InvisibleMan().create();
        }));
    }

    public static void sendMessage(int id, Object... contents) {
        if (conn != null && !conn.isClosed()) {
            conn.sendMessage(id, contents);
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
