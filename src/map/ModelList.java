package map;

import graphics.data.Animation;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import util.Vec2;
import util.Vec3;

/**
 *
 * @author Grant
 */
public class ModelList {

    private static final Map<Vec3, List<Animation>> models = new HashMap();

    public static Map<Vec3, List<Animation>> getAll() {
        return models;
    }

    public static List<Animation> get(Vec3 p) {
        Vec3 o = null;

        for (Vec3 v : models.keySet()) {
            if (v.equals(p)) {
                o = v;
            }
        }

        return models.get(p);
    }

    public static void add(Vec3 pos, String s) {

        Animation anim = new Animation(s, s + "diffuse");

        boolean b = false;
        Vec3 d = null;

        if (models.keySet() == null) {
            List al = new ArrayList();
            al.add(anim);
            models.put(pos, al);
        } else {
            for (Vec3 v : models.keySet()) {
                if (v.equals(pos)) {
                    b = true;
                    d = v;
                }
            }

            if (b) {
                for (Animation a : get(d)) {
                    if (a.name.equals(s)) {
                        return;
                    }
                }
            } else {
                List al = new ArrayList();
                al.add(anim);
                models.put(pos, al);
            }
        }

    }

//        if(models.containsKey(pos)){
//            if(!models.get(pos).contains(new Animation(s,s+"diffuse"))){
//                List al = new ArrayList();
//                al.add(new Animation(s,s+"diffuse"));
//                models.put(pos, al);
//            }
//        } else {
//            List al = new ArrayList();
//            al.add(new Animation(s,s+"diffuse"));
//            models.put(pos, al);
//        }
    public static void remove(Vec3 pos) {

        Vec3 d = null;

        for (Vec3 v : models.keySet()) {

            if (v.equals(pos)) {

                d = v;
            }
        }

        models.remove(d);
    }

    public static void removeAll() {
        models.clear();
    }

    public static void drawAll() {
        models.forEach((p, s) -> {
            s.forEach(m -> {
                m.draw(p.add(new Vec2(.5).toVec3()), 0);
            });
        });
    }

    public static void draw(Vec3 pos) {
        if (!models.containsKey(pos)) {
            return;
        }
        get(pos).forEach(m -> {
            m.draw(pos.add(new Vec2(.5).toVec3()), 0);
        });
//        new Animation(models.get(p),models.get(p)+"diffuse").draw(p.add(new Vec2(.5).toVec3()), 0);
    }

    public static void save(PrintWriter writer) {
        models.forEach((p, s) -> {
            s.forEach(m -> {
                writer.printf("m %f %f %f %s\n", p.x, p.y, p.z, m.name);
            });
        });
    }

}
