package invisibleman;

import engine.Core;
import engine.Destructible;
import engine.Input;
import engine.Signal;
import graphics.Graphics2D;
import graphics.data.Framebuffer;
import graphics.data.Framebuffer.DepthAttachment;
import graphics.data.Framebuffer.TextureAttachment;
import graphics.data.PostProcessEffect;
import graphics.data.Shader;
import network.Connection;
import network.NetworkUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL11.glTranslated;
import util.*;
import static util.Color4.RED;
import static util.Color4.WHITE;

public abstract class Client {

    private static Connection conn;

    public static void main(String[] args) {
        //Try to connect to the server
        if (args.length == 0) {
            conn = NetworkUtils.connectManual();
        } else {
            conn = NetworkUtils.connect(args[0]);
        }

        //Handle messages recieved from the connection
        registerMessageHandlers();

        //Set the game to 3D - this must go before Core.init();
        Core.is3D = true;
        Core.init();

        //Show the fps
        Core.render.bufferCount(Core.interval(1)).forEach(i -> Display.setTitle("FPS: " + i));
        //Hide the mouse
        Mouse.setGrabbed(true);
        //The reset button
        Input.whenKey(Keyboard.KEY_BACKSLASH, true).onEvent(() -> sendMessage(5));

        Shader.clear();
        //Silly graphics effects
        Signal<Integer> cMod = Input.whenMouse(1, true).reduce(0, i -> (i + 1) % 6);
        new PostProcessEffect(10, new Framebuffer(new TextureAttachment(), new DepthAttachment()),
                new Shader("default.vert", "invert.frag")).toggleOn(cMod.map(i -> i == 1));
        new PostProcessEffect(10, new Framebuffer(new TextureAttachment(), new DepthAttachment()),
                new Shader("default.vert", "grayscale.frag")).toggleOn(cMod.map(i -> i == 2));
        Shader wobble = new Shader("default.vert", "wobble.frag");
        Core.time().forEach(t -> wobble.setVec2("wobble", new Vec2(.01 * Math.sin(3 * t), .01 * Math.cos(3.1 * t)).toFloatBuffer()));
        new PostProcessEffect(10, new Framebuffer(new TextureAttachment(), new DepthAttachment()),
                wobble).toggleOn(cMod.map(i -> i == 3));
//        new PostProcessEffect(10, new Framebuffer(new HDRTextureAttachment(), new DepthAttachment()),
//                new Shader("default.vert", "bloom.frag")).toggleOn(cMod.map(i -> i == 4));
        new PostProcessEffect(10, new Framebuffer(new TextureAttachment(), new DepthAttachment()),
                new Shader("default.vert", "gamma.frag")).toggleOn(cMod.map(i -> i == 5));

        //Create the snow particles
        new Snow().create();

        //Create the fog
        new Fog(Color4.gray(.8), .025, .98).create();

        Core.render.onEvent(() -> {
            Fog.setMinTexColor(1, 1, 1, 1);

            //Draw the floor
            glTranslated(0, 0, -.1);
            Graphics2D.fillRect(new Vec2(-200), new Vec2(400), WHITE);
            glTranslated(0, 0, .1);

            //Draw the border
            Graphics2D.drawRect(new Vec2(-20), new Vec2(40), RED);

            Fog.setMinTexColor(0, 0, 0, 0);
        });

        //Create the trees
        for (int i = -15; i <= 15; i += 2) {
            for (int j = -15; j <= 15; j += 2) {
                Tree t = new Tree();
                t.create();
                t.get("position", Vec3.class).set(new Vec3(i * 3, j * 3, 0));
            }
        }

        //Create the player
        new InvisibleMan().create();

        //Start the game
        Core.run();
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
            ThreadManager.onMainThread(() -> new Explosion(pos, RED).create());
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
}
