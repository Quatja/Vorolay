package quatja.com.voronoitest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

import quatja.com.vorolay.VoronoiView;


public class SettingsActivity extends AppCompatActivity {

    VoronoiView voronoiView;
    int region_count = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        LayoutInflater layoutInflater = getLayoutInflater();

        voronoiView = (VoronoiView) findViewById(R.id.voronoi);


        for (int i = 0; i < region_count; i++) {
            View view = layoutInflater.inflate(R.layout.item_voronoi_2, null, false);
            voronoiView.addView(view);

            View layout = view.findViewById(R.id.layout);
            int randomColor = Utils.randomColor();
            layout.setBackgroundColor(randomColor);
        }


        Switch switch_border = (Switch) findViewById(R.id.switch_border);
        switch_border.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    voronoiView.enableBorders(true);
                else
                    voronoiView.enableBorders(false);
            }
        });
        switch_border.setChecked(true);

        Switch switch_border_round = (Switch) findViewById(R.id.switch_border_round);
        switch_border_round.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    voronoiView.enableBorderRoundCorners(true);
                else
                    voronoiView.enableBorderRoundCorners(false);
            }
        });
        switch_border_round.setChecked(true);

        Switch switch_points_order = (Switch) findViewById(R.id.switch_points);
        switch_points_order.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    voronoiView.setGenerationType(VoronoiView.GENERATION_TYPE_ORDERED);
                else
                    voronoiView.setGenerationType(VoronoiView.GENERATION_TYPE_RANDOM);
                voronoiView.refresh();
            }
        });
        switch_border_round.setChecked(false);


        SeekBar seekBar = (SeekBar) findViewById(R.id.seekbar_border_width);
        seekBar.setProgress( (int) voronoiView.getBorderWidth());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int value;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                value = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                voronoiView.setBorderWidth(value);
            }
        });


        Button btnRandomColor = (Button) findViewById(R.id.btn_border_color);
        btnRandomColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int randomColor = Utils.randomColor();
                voronoiView.setBorderColor(randomColor);
            }
        });

        Button btnGenerate = (Button) findViewById(R.id.btn_generate);
        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voronoiView.refresh();
            }
        });
    }
}
