package invisibleman;

import engine.Core;
import engine.Destructible;
import engine.Input;
import graphics.Graphics2D;
import graphics.Window3D;
import network.Connection;
import network.NetworkUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import static org.lwjgl.opengl.ARBTessellationShader.GL_QUADS;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL43.GL_TEXTURE_2D;
import static util.Color4.RED;
import static util.Color4.WHITE;
import util.*;

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
        //Set the background color
        Window3D.background = Color4.gray(.8);
        //Draw the world edges
        Core.render.onEvent(() -> Graphics2D.drawRect(new Vec2(-20), new Vec2(40), RED));
        //The reset button
        Input.whenKey(Keyboard.KEY_BACKSLASH, true).onEvent(() -> sendMessage(5));
        //Draw the ground
        Core.render.onEvent(() -> {
            glDisable(GL_TEXTURE_2D);
            WHITE.glColor();
            int size = 10000;
            glBegin(GL_QUADS);
            glVertex3d(-size, -size, -.1);
            glVertex3d(size, -size, -.1);
            glVertex3d(size, size, -.1);
            glVertex3d(-size, size, -.1);
            glEnd();
        });

        //Create the trees
        for (int i = -15; i <= 15; i += 2) {
            for (int j = -15; j <= 15; j += 2) {
                Tree t = new Tree();
                t.create();
                t.get("position", Vec3.class).set(new Vec3(i * 3, j * 3, 0));
            }
        }

        //Create the snow particles
        new Snow().create();

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
