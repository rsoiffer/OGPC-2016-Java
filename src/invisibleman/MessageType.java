package invisibleman;

import java.util.Arrays;
import util.Vec3;

public enum MessageType {

    FOOTSTEP(Vec3.class, Double.class, Boolean.class, Double.class), //position, rotation, isLeft, opacity
    SMOKE(Vec3.class, Double.class), //position, opacity

    SNOWBALL(Vec3.class, Vec3.class), //position, velocity

    HIT(Vec3.class), //position

    CHAT_MESSAGE(String.class), //the contents of the message

    BLOCK_PLACE(Vec3.class, Integer.class), //position, cube type id

    MAP_FILE(String.class), //map name

    RESTART(); //no information needed

    public final Class[] dataTypes;

    private MessageType(Class... dataTypes) {
        this.dataTypes = dataTypes;
    }

    public int id() {
        return Arrays.asList(values()).indexOf(this);
    }

    public boolean verify(Object... data) {
        if (dataTypes.length != data.length) {
            return false;
        }
        for (int i = 0; i < data.length; i++) {
            if (!dataTypes[i].isInstance(data[i])) {
                return false;
            }
        }
        return true;
    }
}
