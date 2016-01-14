package invisibleman;

import engine.Core;
import graphics.Window3D;
import network.Connection;
import network.NetworkUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import static util.Color4.WHITE;
import util.Vec3;

public abstract class ClientMain {

    public static Connection conn;

    public static void main(String[] args) {
        //Try to connect to the server
        conn = NetworkUtils.connectSimple();

        //Handle messages recieved from the connection
        conn.registerHandler(0, () -> {
            Footstep f = new Footstep();
            f.create();
            f.set(conn.read(Vec3.class), conn.read(Double.class), conn.read(Boolean.class));
        });

        //Set the game to 3D - this must go before Core.init();
        Core.is3D = true;
        Core.init();

        //Show the fps
        Core.update.forEach(t -> Display.setTitle("FPS: " + 1 / t));

        //Hide the mouse
        Mouse.setGrabbed(true);

        //Set the background color
        Window3D.background = WHITE;

        //Create the player
        new InvisibleMan().create();

        //Start the game
        Core.run();
    }
}
