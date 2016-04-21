package map;

import graphics.Graphics2D;
import graphics.data.Texture;
import graphics.loading.SpriteContainer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static util.Color4.*;
import util.Util;
import util.Vec2;

public class CubeType {

    public final String name;
    public final Texture texture;
    public final int id = maxID++;

    private static int maxID = 0;

    private CubeType(String name) {
        this.name = name;
        texture = SpriteContainer.loadSprite("blocks/" + name);
    }

    public void draw(Vec2 pos, double size) {
        Graphics2D.fillRect(pos.subtract(new Vec2(size * .55)), new Vec2(size * 1.1), gray(.5));
        Graphics2D.drawRect(pos.subtract(new Vec2(size * .55)), new Vec2(size * 1.1), BLACK);

        Graphics2D.drawSprite(texture, pos, new Vec2(size / texture.getImageWidth()), 0, WHITE);
        Graphics2D.drawRect(pos.subtract(new Vec2(size * .5)), new Vec2(size), BLACK);

    }

    private static final List<CubeType> TYPES = new ArrayList();
    private static final List<List<CubeType>> GROUPS = new ArrayList();

    public static int distinct() {
        return GROUPS.size();
    }

    public static List<CubeType> getAll() {
        return TYPES;
    }

    public static CubeType getByName(String name) {
        return TYPES.stream().filter(ct -> ct.name.equals(name)).findAny().orElse(null);
    }

    public static CubeType getFirst(int id) {
        return GROUPS.get(id).get(0);
    }

    public static int getGroup(CubeType ct) {
        for (int i = 0; i < distinct(); i++) {
            if (GROUPS.get(i).contains(ct)) {
                return i;
            }
        }
        return -1;
    }

    public static CubeType getRandom(int id) {
        List<CubeType> versions = GROUPS.get(id);
        return versions.get((int) (Math.random() * versions.size()));
    }

    public static boolean hasVersions(int id) {
        return GROUPS.get(id).size() > 1;
    }

    public static CubeType idToType(int id) {
        return id == -1 ? null : TYPES.get(id);
    }

    public static int typeToId(CubeType type) {
        return type == null ? -1 : TYPES.indexOf(type);
    }

    static {
        try {
            Files.readAllLines(Paths.get("block_list.txt")).forEach(s -> {
                if (!s.isEmpty()) {
                    if (!s.contains(" ")) {
//                        if(s.startsWith("model_")){
//                            
//                        } else {
                            CubeType ct = new CubeType(s);
                            TYPES.add(ct);
                            GROUPS.add(Arrays.asList(ct));
//                        }
                    } else {
                        String[] a = s.split(" ");
                        ArrayList<CubeType> versions = new ArrayList();
                        Util.repeat(Integer.parseInt(a[1]), i -> versions.add(new CubeType(a[0] + "_" + (i + 1))));
                        TYPES.addAll(versions);
                        GROUPS.add(versions);
                    }
                }
            });
        } catch (IOException ex) {
        }
    }
}
