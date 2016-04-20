package invisibleman;

import engine.Signal;
import graphics.data.Animation;
import util.Vec3;

/**
 *
 * @author Grant
 */
public class Model {

    public String modelname;
    public String diffusename;
    public Vec3 pos;

    public Model(String mname, String dname, Vec3 p) {
        modelname = mname;
        diffusename = dname;
        pos = p;
    }

    public Signal<Animation> getAnimation() {
        return new Signal(new Animation(modelname, diffusename));
    }

}
