package quatja.com.voronoitest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import quatja.com.vorolay.VoronoiView;

public class SimpleListActivity extends AppCompatActivity {

    LayoutInflater layoutInflater;

    String[] elems = {
            "Element 1", "Element 2", "Element 3", "Element 4", "Element 5",
            "Element 6", "Element 7", "Element 8", "Element 9", "Element 10",
            "Element 11", "Element 12"
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_list);
        layoutInflater = getLayoutInflater();

        ListView listview = (ListView) findViewById(R.id.listview);
        listview.setAdapter(new MyAdapter());
    }


    class MyAdapter extends BaseAdapter {
        public int getCount() {
            return elems.length;
        }

        public Object getItem(int position) {
            return elems[position];
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listview, parent, false);
                holder = new ViewHolder();
                convertView.setTag(holder);

                holder.voronoi = (VoronoiView) convertView.findViewById(R.id.voronoi);

                for (int i = 0; i < 20; i++) {
                    View view = layoutInflater.inflate(R.layout.item_voronoi_2, null, false);
                    holder.voronoi.addView(view);

                    View layout = view.findViewById(R.id.layout);
                    int randomColor = Utils.randomColor();
                    layout.setBackgroundColor(randomColor);
                }
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.textView = (TextView) convertView.findViewById(R.id.elem_text);


            String string = elems[position];
            holder.textView.setText(string);
            holder.textView.setVisibility(View.VISIBLE);




            return convertView;
        }
    }

    static class ViewHolder {
        VoronoiView voronoi;
        TextView textView;
    }

}
