package example.vorolay.consumer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import quatja.com.vorolay.VoronoiView;

public final class MainActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.main);
        VoronoiView view = findViewById(R.id.voronoi);
        view.setGenerationType(VoronoiView.GENERATION_TYPE_ORDERED);
        view.addView(new View(this));
    }
}
