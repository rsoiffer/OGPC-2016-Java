package networking;

import engine.Core;
import engine.Destructible;
import game.*;
import gui.GUIController;
import gui.TypingManager;
import guis.*;
import java.util.Arrays;
import java.util.function.Consumer;
import map.CubeMap;
import map.CubeType;
import network.Connection;
import network.NetworkUtils;
import static networking.MessageType.*;
import org.lwjgl.opengl.Display;
import util.*;

public abstract class Client {

    public static boolean IS_MULTIPLAYER = true;
    private static Connection conn;
    public static Chat con;

    public static void main(String[] args) {
        //Set the game to 3D - this must go before Core.init();
        Core.is3D = true;
        Core.init();

        //Show the fps
        Core.render.bufferCount(Core.interval(1)).forEach(i -> Display.setTitle("FPS: " + i));

        MiniChat mc = new MiniChat("mChat++");
        Play ps = new Play("level select", new Vec2(Core.screenWidth, Core.screenHeight));
        Join jn = new Join("ip select");

        TitleScreen ts = new TitleScreen("main menu", new Vec2(Core.screenWidth, Core.screenHeight));

        TypingManager tpm = new TypingManager(ts);
        GUIController.add(ts, jn, ps, mc);

        //Sounds.playSound("ethereal.mp3", true, .05);
        Core.update.onEvent(GUIController::update);
        Core.renderLayer(100).onEvent(GUIController::draw);

        //Start the game
        ts.start();
        Core.run();

        //Force the program to stop
        System.exit(0);
    }

    public static void connect(String ip) {

        if (IS_MULTIPLAYER) {
            //Try to connect to the server
            //if (args.length == 0) {
            conn = NetworkUtils.connect(ip);
            //} else {
            //    conn = NetworkUtils.connect(args[0]);
            //}

            //Handle messages recieved from the connection
            registerMessageHandlers();

            sendMessage(GET_NAME, "NewPlayer");
            Core.timer(.5, conn::open);
        }
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

        handleMessage(GET_NAME, data -> {

            Game.setName((String) data[0]);
        });

        handleMessage(SCORE, data -> {

            ((Score) GUIController.getGUI("scorb")).point((String) data[0]);
        });

        handleMessage(FOOTSTEP, data -> {
            Footstep f = new Footstep();
            f.create();
            f.set((Vec3) data[0], (double) data[1], (boolean) data[2], (double) data[3]);
        });

        handleMessage(SMOKE, data -> {
            Smoke s = new Smoke();
            s.create();
            s.get("position", Vec3.class).set((Vec3) data[0]);
            s.get("opacity", Double.class).set((double) data[1]);
        });

        handleMessage(SNOWBALL, data -> {
            BallAttack b = new BallAttack();
            b.create();
            b.get("position", Vec3.class).set((Vec3) data[0]);
            b.get("velocity", Vec3.class).set((Vec3) data[1]);
            b.isEnemy = true;
            b.thrower = (int) data[2];
        });

        handleMessage(HIT, data -> {
            Particle.explode((Vec3) data[0], new Color4(0, .5, 1));
            Sounds.playSound("hit.wav");
        });

        handleMessage(CHAT_MESSAGE, data -> {
            con.addChat((String) data[0]);
        });

        handleMessage(BLOCK_PLACE, data -> {
            Vec3 coords = (Vec3) data[0];
            CubeMap.setCube((int) coords.x, (int) coords.y, (int) coords.z, CubeType.idToType((int) data[1]));
        });

        handleMessage(MAP_FILE, data -> {
            CubeMap.load("levels/level_" + data[0] + ".txt");
        });

        handleMessage(RESTART, data -> {
            RegisteredEntity.getAll(BallAttack.class, Footstep.class, Smoke.class, InvisibleMan.class).forEach(Destructible::destroy);
            Particle.clear();
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
}
