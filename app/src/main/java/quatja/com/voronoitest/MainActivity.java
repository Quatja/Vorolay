package quatja.com.voronoitest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnSimple = (Button) findViewById(R.id.btn_simple_diagram);
        btnSimple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SimpleDiagramActivity.class);
                startActivity(intent);
            }
        });

        Button btnSettings = (Button) findViewById(R.id.btn_settings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        Button btnSimpleList = (Button) findViewById(R.id.btn_simple_list);
        btnSimpleList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SimpleListActivity.class);
                startActivity(intent);
            }
        });

        Button btnCustomRegions = (Button) findViewById(R.id.btn_custom_regions);
        btnCustomRegions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CustomRegionsActivity.class);
                startActivity(intent);
            }
        });

        Button btnAddRegion = (Button) findViewById(R.id.btn_add_region);
        btnAddRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddRegionsActivity.class);
                startActivity(intent);
            }
        });

    }


}
