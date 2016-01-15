package invisibleman;

import java.util.LinkedList;
import java.util.List;
import network.Connection;
import network.NetworkUtils;
import util.Log;
import util.Vec3;

public class Server {

    private static class ClientInfo {

        static int maxID = 0;

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
            ClientInfo info = new ClientInfo(conn, ClientInfo.maxID++);
            clients.add(info);

            Log.print("Client " + info.id + " connected");
            conn.onClose(() -> {
                clients.remove(info);
                Log.print("Client " + info.id + " disconnected");
            });

            conn.registerHandler(0, () -> {
                System.out.println("footstep");
                Vec3 pos = conn.read(Vec3.class);
                double rot = conn.read(Double.class);
                boolean isLeft = conn.read(Boolean.class);
                clients.stream().filter(ci -> ci != info).forEach(ci -> ci.conn.sendMessage(0, pos, rot, isLeft));
            });
        }).start();
    }
}
