package invisibleman;

import java.util.*;
import network.Connection;
import network.NetworkUtils;
import util.Log;
import util.Vec3;

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

    public static void main(String[] args) {
        List<ClientInfo> clients = new LinkedList();
        NetworkUtils.server(conn -> {
            ClientInfo info = new ClientInfo(conn);
            clients.add(info);

            Log.print("Client " + info.id + " connected");
            conn.onClose(() -> {
                clients.remove(info);
                Log.print("Client " + info.id + " disconnected");
            });

            relay(info, clients, 0, Vec3.class, Double.class, Boolean.class);
            relay(info, clients, 1, Vec3.class, Vec3.class);
            relay(info, clients, 2, Vec3.class);
            relayAll(info, clients, 5);
        }).start();

        registerCommand(() -> System.exit(0), "close", "end", "exit", "stop", "q", "quit");
        
        registerCommand(() -> {
            System.out.println("Client list:");
            clients.forEach(System.out::println);
        }, "all", "clients", "connected", "list", "players");
        
        registerCommand(() -> Client.sendMessage(3, new Vec3(0, 0, 1)), "snowball");
        
        registerCommand(() -> clients.forEach(ci -> ci.conn.sendMessage(5)), "new game", "restart", "start");
        startCommandLine();
    }

    private static void relay(ClientInfo info, List<ClientInfo> clients, int id, Class... contents) {
        info.conn.registerHandler(id, () -> {
            Object[] data = new Object[contents.length];
            for (int i = 0; i < data.length; i++) {
                data[i] = info.conn.read(contents[i]);
            }
            clients.stream().filter(ci -> ci != info).forEach(ci -> ci.conn.sendMessage(id, () -> Arrays.asList(data).forEach(ci.conn::write)));
        });
    }

    private static void relayAll(ClientInfo info, List<ClientInfo> clients, int id, Class... contents) {
        info.conn.registerHandler(id, () -> {
            Object[] data = new Object[contents.length];
            for (int i = 0; i < data.length; i++) {
                data[i] = info.conn.read(contents[i]);
            }
            clients.forEach(ci -> ci.conn.sendMessage(id, () -> Arrays.asList(data).forEach(ci.conn::write)));
        });
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
}
