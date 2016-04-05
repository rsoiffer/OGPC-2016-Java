package map;

public class CubeData {

    public final int x, y, z;
    public final CubeType c;

    public CubeData(int x, int y, int z, CubeType c) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.c = c;
    }

    @Override
    public String toString() {
        return "CubeData{" + "x=" + x + ", y=" + y + ", z=" + z + ", c=" + c + '}';
    }
}
