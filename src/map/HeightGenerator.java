/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package map;

/**
 * Used to generate and store height maps.
 * @author Cruz
 */
public class HeightGenerator {

    private int[][] map;
    private final int size;
    private int[] warpX;
    private int[] warpY;
    private int iterations;

    /**
     * Makes a new square height map.
     * @param s The size of the new height map.
     * @param i The number of iterations of smooth().
     */
    public HeightGenerator(int s, int i) {
        
        warpX = new int[]{-1, 0, 1, -1, 1, -1, 0, 1};
        warpY = new int[]{-1, -1, -1, 0, 0, 1, 1, 1};
        size = s;
        map = new int[s][s];
        iterations = i;
    }

    /**
     * Creates a new height map, accessible using getMap(). 
     */
    public void generate() {

        double[][] m = new double [size][size];
        shuffle(m);

        for (int i = 0; i < iterations; i++) {

            smooth(m);
        }

        double[] ext = extrema(m);
//        grain(m, ext);
//        ext = extrema(m);
        regulate(m, ext);
        
        for (int i = 0; i < map.length; i++) {
            
            for (int j = 0; j < map[0].length; j++) {
                
                map[i][j] = (int) m[i][j];
            }
        }
    }
    
    public void setIterations(int i){
        
        iterations = i;
    }
    
    public void setAveArea(int[] wx, int[] wy){
        
        warpX = wx;
        warpY = wy;
    }
    
    private double[] extrema(double[][] m){
        
        double[] ext = new double[2];
        ext[0] = m[0][0];
        ext[1] = m[0][0];

        for (int i = 0; i < size; i++) {

            for (int j = 0; j < size; j++) {

                if (m[i][j] < ext[0]) {

                    ext[0] = m[i][j];
                } else if (m[i][j] > ext[1]) {

                    ext[1] = m[i][j];
                }
            }
        }
        
        return ext;
    }
    
    private void grain(double[][] m, double[] e){
        
        for (int i = 0; i < size; i++) {
            
            for (int j = 0; j < size; j++) {
                
                m[i][j] += (Math.random() - 0.5) * 4 * (e[1] - e[0]) / size;
            }
        }
    }

    private void regulate(double[][] m, double[] e) {

        double dis = 127.0 - ((e[1] - e[0]) / 2.0 + e[0]);
        double mult = 127.0 / Math.abs(e[1] - 127.0 + dis);

        for (int i = 0; i < size; i++) {

            for (int j = 0; j < size; j++) {

                double scale = m[i][j] + dis - 127.0;
                m[i][j] = scale * mult + 127.0;
            }
        }
    }

    private void smooth(double[][] m) {

        double[][] ref = m;

        for (int i = 0; i < size; i++) {

            for (int j = 0; j < size; j++) {

                double sum = ref[i][j];

                for (int k = 0; k < warpX.length; k++) {

                    int indX = (warpX[k] + i + size) % size;
                    int indY = (warpY[k] + j + size) % size;
                    sum += ref[indX][indY];
                }

                m[i][j] = sum / (warpX.length + 1);
            }
        }
    }

    private void shuffle(double[][] m) {

        for (int i = 0; i < size; i++) {

            for (int j = 0; j < size; j++) {

                m[i][j] = Math.floor(Math.random() * 256);
            }
        }
    }

    private int[][] toIntMap(double[][] m) {
        
        int[][] nMap = new int[size][size];
        
        for (int i = 0; i < size; i++) {

            for (int j = 0; j < size; j++) {

                nMap[i][j] = (int) m[i][j];

                if (nMap[i][j] < 0) {

                    nMap[i][j] = 0;
                } else if (nMap[i][j] > 255) {

                    nMap[i][j] = 255;
                }
            }
        }
        
        return nMap;
    }

    public int[][] getMap() {
        
        return map;
    }
}
