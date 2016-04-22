package map;

import graphics.data.Animation;
import java.io.PrintWriter;
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
    
    private static final Map<Vec3,List<Animation>> models = new HashMap();
    
    public static Map<Vec3,List<Animation>> getAll(){
        return models;
    }
    
    public static List<Animation> get(Vec3 p){
        return models.get(p);
    }
    
    public static void add(Vec3 pos, String s){
        if(!get(pos).contains(new Animation(s,s+"diffuse"))){
            List al = new ArrayList();
            al.add(new Animation(s,s+"diffuse"));
            models.put(pos, al);
        }
    }
    
    public static void remove(Vec3 pos) {
        models.remove(pos);
    }
    
    public static void drawAll(){
        models.forEach((p,s) -> {
            s.forEach(m -> {
                m.draw(p.add(new Vec2(.5).toVec3()), 0);
            });
        });
    }
    
    public static void draw(Vec3 pos){
        
        models.get(pos).forEach(m -> {
            m.draw(pos.add(new Vec2(.5).toVec3()), 0);
        });
//        new Animation(models.get(p),models.get(p)+"diffuse").draw(p.add(new Vec2(.5).toVec3()), 0);
    }
    
    public static void load(PrintWriter writer){
        
    }
    
    public static void save(PrintWriter writer){
        models.forEach((p,s) -> {
            s.forEach(m -> {
                writer.printf("m %f %f %f %s\n", p.x, p.y, p.z, m.name);
            });
        });
    }
    
}
