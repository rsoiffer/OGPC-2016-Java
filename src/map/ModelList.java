package map;

import graphics.data.Animation;
import graphics.data.Texture;
import graphics.loading.SpriteContainer;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.Vec2;
import util.Vec3;

/**
 *
 * @author Grant
 */
public class ModelList {

    private static final Map<Vec3, List<Animation>> modelmap = new HashMap();
    private static final ArrayList<Animation> models;

    static {
        models = new ArrayList();
        try {
            List<String> lines = Files.readAllLines(Paths.get("model_list.txt"));
            lines.forEach(l -> {
                models.add(new Animation(l, l + "diffuse"));
            });
        } catch (Exception e) {

        }
    }

    public static Map<Vec3, List<Animation>> getMap() {
        return modelmap;
    }

    public static ArrayList<Animation> getAll() {
        return models;
    }

    public static List<Animation> get(Vec3 p) {
        Vec3 o = null;

        for (Vec3 v : modelmap.keySet()) {
            if (v.equals(p)) {
                o = v;
            }
        }

        return modelmap.get(p);
    }

    public static void add(Vec3 pos, String s) {

        int i = getId(s);

        if (i == -1) {
            models.add(new Animation(s, s + "diffuse"));
        } else {
            for (Vec3 v : modelmap.keySet()) {
                if (pos.equals(v)) {
                    pos = v;
                    break;
                }
            }
            if (get(pos) != null) {
                if (!get(pos).contains(models.get(i))) {
                    get(pos).add(models.get(i));
                }
            } else {
                ArrayList al = new ArrayList();
                al.add(models.get(i));
                modelmap.put(pos, al);
            }
        }
//        Animation anim = new Animation(s, s + "diffuse");
//
//        boolean b = false;
//        Vec3 d = null;
//
//        if (modelmap.keySet() == null) {
//            List al = new ArrayList();
//            al.add(anim);
//            modelmap.put(pos, al);
//        } else {
//            for (Vec3 v : modelmap.keySet()) {
//                if (v.equals(pos)) {
//                    b = true;
//                    d = v;
//                }
//            }
//
//            if (b) {
//                for (Animation a : get(d)) {
//                    if (a.name.equals(s)) {
//                        return;
//                    }
//                }
//            } else {
//                List al = new ArrayList();
//                al.add(anim);
//                modelmap.put(pos, al);
//            }
//        }

    }

//        if(modelmap.containsKey(pos)){
//            if(!modelmap.get(pos).contains(new Animation(s,s+"diffuse"))){
//                List al = new ArrayList();
//                al.add(new Animation(s,s+"diffuse"));
//                modelmap.put(pos, al);
//            }
//        } else {
//            List al = new ArrayList();
//            al.add(new Animation(s,s+"diffuse"));
//            modelmap.put(pos, al);
//        }
    public static void add(Vec3 pos, int i) {

        for (Vec3 v : modelmap.keySet()) {
            if (pos.equals(v)) {
                pos = v;
                break;
            }
        }
        if (get(pos) != null) {
            if (!get(pos).contains(models.get(i))) {
                get(pos).add(models.get(i));
            }
        } else {
            ArrayList al = new ArrayList();
            al.add(models.get(i));
            modelmap.put(pos, al);
        }
    }

    public static void remove(Vec3 pos) {

        Vec3 d = null;

        for (Vec3 v : modelmap.keySet()) {

            if (v.equals(pos)) {

                d = v;
            }
        }

        modelmap.remove(d);
    }

    public static void removeAll() {
        modelmap.clear();
    }

    public static void drawAll() {
        modelmap.forEach((p, s) -> {
            s.forEach(m -> {
                m.draw(p.add(new Vec2(.5).toVec3()), 0);
            });
        });
    }

    public static void draw(Vec3 pos) {
        if (!modelmap.containsKey(pos)) {
            return;
        }
        get(pos).forEach(m -> {
            m.draw(pos.add(new Vec2(.5).toVec3()), 0);
        });
//        new Animation(modelmap.get(p),modelmap.get(p)+"diffuse").draw(p.add(new Vec2(.5).toVec3()), 0);
    }

    public static void save(PrintWriter writer) {
        modelmap.forEach((p, s) -> {
            s.forEach(m -> {
                writer.printf("m %f %f %f %s\n", p.x, p.y, p.z, m.name);
            });
        });
    }

    public static int getId(Animation a) {
        for (Animation m : models) {
            if (m.equals(a)) {
                return models.indexOf(m);
            }
        }
        return -1;
    }

    public static int getId(String s) {
        for (Animation m : models) {
            if (m.name.equals(s)) {
                return models.indexOf(m);
            }
        }
        return -1;
    }

    public static Texture getIcon(int id) {
        return SpriteContainer.loadSprite(models.get(id).name + "icon");
    }

}
