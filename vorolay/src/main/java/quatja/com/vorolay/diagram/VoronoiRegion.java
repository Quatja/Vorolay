package quatja.com.vorolay.diagram;


import android.graphics.Path;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

import quatja.com.vorolay.GrahamScan;

/**
 *
 * Voronoi region allows to work with each diagram element
 *
 * Created by quatja
 */
public class VoronoiRegion {
    List<VoronoiLine> edges = new ArrayList<>();
    VoronoiPoint site;
    public int tag;
    double screen_width, screen_height;

    private List<VoronoiPoint> points = new ArrayList<>();
    public Path path;

    public double width, height;
    public VoronoiPoint center_rect;

    void prepare() {

        initPoints();
        initPath();

        prepareWidth();
        prepareHeight();

        prepareCenter();
    }




    private void initPoints() {
        for (VoronoiLine edge : edges) {
            VoronoiPoint point = new VoronoiPoint(edge.x1, edge.y1);
            VoronoiPoint point2 = new VoronoiPoint(edge.x2, edge.y2);

            if (!points.contains(point))
                points.add(point);
            if (!points.contains(point2))
                points.add(point2);
        }

        // Sort
        points = GrahamScan.getConvexHull(points);
    }

    private void initPath() {
        path = new Path();

        for (int i = 0; i < points.size(); i++) {
            VoronoiPoint point = points.get(i);
            if (i == 0) {
                path.moveTo((float)point.x, (float)point.y);
                continue;
            }
            path.lineTo((float)point.x, (float)point.y);
        }

        path.close();
    }

    private void prepareWidth() {
        double min = points.get(0).x;
        double max = points.get(0).x;

        for (int i = 1; i < points.size(); i++) {
            VoronoiPoint p1 = points.get(i);
            if (min > p1.x)
                min = p1.x;
            if (max < p1.x)
                max = p1.x;

        }

        width = max - min;
    }

    private void prepareHeight() {
        double min = points.get(0).y;
        double max = points.get(0).y;

        for (int i = 1; i < points.size(); i++) {
            VoronoiPoint p1 = points.get(i);
            if (min > p1.y)
                min = p1.y;
            if (max < p1.y)
                max = p1.y;

        }

        height = max - min;
    }

    private void prepareCenter() {
        RectF mRectF = new RectF();
        path.computeBounds(mRectF, true);
        center_rect = new VoronoiPoint(mRectF.centerX(), mRectF.centerY());
    }

    public boolean contains(float x, float y) {
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = points.size() - 1; i < points.size(); j = i++) {
            if ((points.get(i).y > y) != (points.get(j).y > y) &&
                    (x < (points.get(j).x - points.get(i).x) * (y - points.get(i).y) / (points.get(j).y-points.get(i).y) + points.get(i).x)) {
                result = !result;
            }
        }
        return result;
    }




    static class VoronoiLine {
        int x1, x2, y1, y2;

        VoronoiLine(int x1, int x2, int y1, int y2) {
            this.x1 = x1;
            this.x2 = x2;
            this.y1 = y1;
            this.y2 = y2;
        }

        @Override
        public String toString() {
            return x1 + ", " + y1 + ";  " + x2 + ", " + y2;
        }
    }

    static public class VoronoiPoint {
        public double x, y;

        public VoronoiPoint() {}

        public VoronoiPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void setPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof VoronoiPoint))
                return false;
            VoronoiPoint point2 = (VoronoiPoint) obj;
            return point2.x == this.x && point2.y == this.y;
        }

        @Override
        public String toString() {
            return x + ", " + y;
        }
    }
}
