package invisibleman;

import java.util.LinkedList;
import java.util.List;
import network.Connection;
import network.NetworkUtils;
import util.Log;
import util.Vec3;

public class ServerMain {

    private static class ClientInfo {

        Connection conn;
        int id;

        public ClientInfo(Connection conn, int id) {
            this.conn = conn;
            this.id = id;
        }
    }

    public static void main(String[] args) {
        List<ClientInfo> clients = new LinkedList();
        NetworkUtils.server(conn -> {
            ClientInfo info = new ClientInfo(conn, clients.size());
            clients.add(info);

            Log.print("Client " + info.id + " connected");
            conn.onClose(() -> Log.print("Client " + info.id + " disconnected"));

            conn.registerHandler(0, () -> {
                Vec3 pos = conn.read(Vec3.class);
                double rot = conn.read(Double.class);
                boolean isLeft = conn.read(Boolean.class);
                clients.stream().filter(ci -> ci != info).forEach(ci -> ci.conn.sendMessage(0, pos, rot, isLeft));
            });

//            conn.defaultHandler = id -> clients.stream().filter(ci -> ci != info).forEach(ci -> ci.conn.write(id));
//            conn.defaultHandler = id -> System.out.print(id + " ");
        }).start();
    }
}
