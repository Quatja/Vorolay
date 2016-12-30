package quatja.com.voronoitest;


import android.graphics.Color;

import java.util.Random;

public class Utils {
    static private Random rand = new Random();

    static public int randomColor() {
        int r = rand.nextInt(255);
        int g = rand.nextInt(255);
        int b = rand.nextInt(255);
        return Color.rgb(r,g,b);
    }
}
