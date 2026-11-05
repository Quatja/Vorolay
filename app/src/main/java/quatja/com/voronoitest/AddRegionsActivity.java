package quatja.com.voronoitest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import quatja.com.vorolay.VoronoiView;


public class AddRegionsActivity extends BaseActivity {

    private LayoutInflater layoutInflater;
    private VoronoiView voronoiView;

    private int count = 16;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_regions);
        layoutInflater = getLayoutInflater();


        voronoiView = (VoronoiView) findViewById(R.id.voronoi);
        Button mainButton = (Button) findViewById(R.id.main_button);

        for (int i = 0; i < count; i++) {
            View view = layoutInflater.inflate(R.layout.item_voronoi_2, voronoiView, false);
            voronoiView.addView(view);

            View layout = view.findViewById(R.id.layout);
            int randomColor = Utils.randomColor();
            layout.setBackgroundColor(randomColor);

            TextView textView = (TextView) view.findViewById(R.id.text);
            textView.setText(""+i);
            textView.setVisibility(View.VISIBLE);
        }


        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View view = layoutInflater.inflate(R.layout.item_voronoi_2, voronoiView, false);

                View layout = view.findViewById(R.id.layout);
                int randomColor = Utils.randomColor();
                layout.setBackgroundColor(randomColor);

                count++;
                TextView textView = (TextView) view.findViewById(R.id.text);
                textView.setText(""+count);
                textView.setVisibility(View.VISIBLE);

                voronoiView.addView(view);
                voronoiView.refresh();
            }
        });

    }
}
