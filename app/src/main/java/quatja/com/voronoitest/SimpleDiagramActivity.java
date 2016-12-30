package quatja.com.voronoitest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import quatja.com.vorolay.VoronoiView;


public class SimpleDiagramActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_diagram);
        LayoutInflater layoutInflater = getLayoutInflater();

        VoronoiView voronoiView = (VoronoiView) findViewById(R.id.voronoi);

        for (int i = 0; i < 15; i++) {
            View view = layoutInflater.inflate(R.layout.item_voronoi_2, null, false);
            voronoiView.addView(view);

            View layout = view.findViewById(R.id.layout);
            int randomColor = Utils.randomColor();
            layout.setBackgroundColor(randomColor);
        }

        voronoiView.setOnRegionClickListener(new VoronoiView.OnRegionClickListener() {
            @Override
            public void onClick(View view, int position) {
                Toast.makeText(SimpleDiagramActivity.this, "position: " + position, Toast.LENGTH_SHORT).show();
            }
        });

    }
}
