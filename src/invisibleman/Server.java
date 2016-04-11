package invisibleman;

import engine.*;
import graphics.Camera;
import graphics.Graphics2D;
import graphics.Window3D;
import static graphics.Window3D.*;
import static invisibleman.MessageType.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import map.CubeMap;
import static map.CubeMap.WORLD_SIZE;
import network.Connection;
import network.NetworkUtils;
import org.lwjgl.input.Keyboard;
import static org.lwjgl.input.Keyboard.*;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import static util.Color4.RED;
import util.*;

public class Server {

    private static class ClientInfo {

        static int maxID = 0;

        Connection conn;
        int id = maxID++;

        public ClientInfo(Connection conn) {
            this.conn = conn;
        }

        @Override
        public String toString() {
            return "Client " + id + ": " + conn;
        }
    }

    private static final List<ClientInfo> CLIENTS = new LinkedList();

    public static void main(String[] args) {
        NetworkUtils.server(conn -> {
            ClientInfo info = new ClientInfo(conn);
            CLIENTS.add(info);

            Log.print("Client " + info.id + " connected");
            conn.onClose(() -> {
                CLIENTS.remove(info);
                Log.print("Client " + info.id + " disconnected");
            });

            handleMessage(info, FOOTSTEP, a -> {
                Footstep f = new Footstep();
                f.create();
                f.set((Vec3) a[0], (double) a[1], (boolean) a[2], (double) a[3]);
                sendToOthers(info, FOOTSTEP, a);
            });
            handleMessage(info, SNOWBALL, a -> {
                BallAttack b = new BallAttack();
                b.create();
                b.get("position", Vec3.class).set((Vec3) a[0]);
                b.get("velocity", Vec3.class).set((Vec3) a[1]);
                b.isEnemy = true;
                sendToOthers(info, SNOWBALL, a);
            });
            handleMessage(info, HIT, a -> {
                new Explosion((Vec3) a[0], new Color4(1, 0, 0)).create();
                Sounds.playSound("hit.wav");
                sendToOthers(info, HIT, a);
            });
            handleMessage(info, SMOKE, a -> {
                Smoke f = new Smoke();
                f.create();
                f.get("position", Vec3.class).set((Vec3) a[0]);
                f.get("opacity", Double.class).set((double) a[1]);
                sendToOthers(info, SMOKE, a);
            });
            handleMessage(info, RESTART, a -> {
                RegisteredEntity.getAll(BallAttack.class, Explosion.class, Footstep.class, Smoke.class, InvisibleMan.class).forEach(Destructible::destroy);
                sendToAll(RESTART, a);
            });

            RegisteredEntity.getAll(Footstep.class).forEach(f -> {
                sendTo(info, FOOTSTEP, f.get("position", Vec3.class).get(), f.get("rotation", Double.class).get(), f.get("isLeft", Boolean.class).get(), f.get("opacity", Double.class).get());
            });
            RegisteredEntity.getAll(Smoke.class).forEach(s -> {
                sendTo(info, SMOKE, s.get("position", Vec3.class).get(), s.get("opacity", Double.class).get());
            });
        }).start();

        //runCommandInterface();
        runGraphicalInterface();
    }

    private static void handleMessage(ClientInfo info, MessageType type, Consumer<Object[]> handler) {
        info.conn.registerHandler(type.id(), () -> {
            Object[] data = new Object[type.dataTypes.length];
            for (int i = 0; i < type.dataTypes.length; i++) {
                data[i] = info.conn.read(type.dataTypes[i]);
            }
            ThreadManager.onMainThread(() -> handler.accept(data));
        });
    }

    private static void sendTo(ClientInfo info, MessageType type, Object... data) {
        if (!info.conn.isClosed()) {
            if (!type.verify(data)) {
                throw new RuntimeException("Data " + Arrays.toString(data) + " does not fit message type " + type);
            }
            info.conn.sendMessage(type.id(), data);
        }
    }

    private static void sendToAll(MessageType type, Object... data) {
        CLIENTS.forEach(ci -> sendTo(ci, type, data));
    }

    private static void sendToOthers(ClientInfo info, MessageType type, Object... data) {
        CLIENTS.stream().filter(ci -> ci != info).forEach(ci -> sendTo(ci, type, data));
    }

    private static void runCommandInterface() {
        registerCommand(() -> System.exit(0), "close", "end", "exit", "stop", "q", "quit");

        registerCommand(() -> {
            System.out.println("Client list:");
            CLIENTS.forEach(System.out::println);
        }, "all", "clients", "connected", "list", "players");

        registerCommand(() -> CLIENTS.forEach(ci -> ci.conn.sendMessage(5)), "new game", "restart", "start");
        startCommandLine();
    }

    private static final Map<String, Runnable> commands = new HashMap();

    private static void registerCommand(Runnable command, String... names) {
        for (String name : names) {
            commands.put(name.toLowerCase(), command);
        }
    }

    private static void startCommandLine() {
        Scanner in = new Scanner(System.in);
        while (true) {
            String s = in.nextLine().toLowerCase();
            if (commands.containsKey(s)) {
                commands.get(s).run();
            } else {
                System.out.println("Command not recognized");
            }
        }
    }

    private static void runGraphicalInterface() {
        //Initial graphics setup
        Core.is3D = true;
        Core.init();
        Core.render.bufferCount(Core.interval(1)).forEach(i -> Display.setTitle("FPS: " + i));
        new Fog(new Color4(.95, .8, .3), .00025, 1).create();
        Mouse.setGrabbed(true);

        //Draw world
        Core.render.onEvent(() -> CubeMap.drawAll());

        //The reset button
        Input.whenKey(Keyboard.KEY_BACKSLASH, true).onEvent(() -> {
            sendToAll(RESTART);
            RegisteredEntity.getAll(BallAttack.class, Explosion.class, Footstep.class, Smoke.class, InvisibleMan.class).forEach(Destructible::destroy);
        });

        //Draw GUI
        Core.renderLayer(100).onEvent(() -> {
            Camera.setProjection2D(new Vec2(0), new Vec2(1200, 800));

            Graphics2D.drawEllipse(new Vec2(600, 400), new Vec2(10), RED, 20);

            if (CubeMap.isSolid(pos)) {
                Graphics2D.drawText("Inside Block", new Vec2(542, 350));
                Graphics2D.fillRect(new Vec2(0), new Vec2(1200, 800), RED.withA(.4));
            }

            Window3D.resetProjection();
        });

        //Movement
        Premade3D.makeMouseLook(new AbstractEntity.LAE(e -> {
        }), 2, -1.5, 1.5);
        Signal<Boolean> fast = Input.whenKey(KEY_TAB, true).reduce(false, b -> !b);
        Supplier<Double> speed = fast.map(b -> b ? 30. : 5);
        Input.whileKeyDown(KEY_W).forEach(dt -> pos = pos.add((fast.get() ? facing.toVec3() : forwards()).multiply(speed.get() * dt)));
        Input.whileKeyDown(KEY_S).forEach(dt -> pos = pos.add((fast.get() ? facing.toVec3() : forwards()).multiply(-speed.get() * dt)));
        Input.whileKeyDown(KEY_A).forEach(dt -> pos = pos.add(facing.toVec3().cross(UP).withLength(-speed.get() * dt)));
        Input.whileKeyDown(KEY_D).forEach(dt -> pos = pos.add(facing.toVec3().cross(UP).withLength(speed.get() * dt)));
        Input.whileKeyDown(KEY_SPACE).forEach(dt -> pos = pos.add(UP.multiply(speed.get() * dt)));
        Input.whileKeyDown(KEY_LSHIFT).forEach(dt -> pos = pos.add(UP.multiply(-speed.get() * dt)));

        //Setup the level
        pos = WORLD_SIZE.multiply(.5);
        CubeMap.load("level3.txt");

        //Attack
        Input.whenMouse(0, true).onEvent(() -> {
            Vec3 vel = Window3D.facing.toVec3().withLength(30);

            sendToAll(SNOWBALL, pos, vel);

            BallAttack b = new BallAttack();
            b.create();
            b.get("position", Vec3.class).set(pos);
            b.get("velocity", Vec3.class).set(vel);
        });

        Core.run();
    }
}
