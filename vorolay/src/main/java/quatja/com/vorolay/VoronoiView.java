package quatja.com.vorolay;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Random;

import quatja.com.vorolay.diagram.Voronoi;
import quatja.com.vorolay.diagram.VoronoiRegion;
import quatja.com.vorolay.diagram.VoronoiRegion.VoronoiPoint;

/**
 * The VoronoiView class allows you to display any ({@link android.view.View}) in
 * Voronoi diagram regions.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Voronoi_diagram">Check wiki</a>
 * <p>
 * Created by quatja
 */
public class VoronoiView extends ViewGroup {

    /**
     * Diagram sites are generating randomly inside the view bounds.
     * Use with {@link #setGenerationType} and {@code app:generation_type}
     */
    final static public int GENERATION_TYPE_RANDOM = 1;
    /**
     * Diagram sites are generating like a grid-view with random offsets
     * Use with {@link #setGenerationType} and {@code app:generation_type}
     */
    final static public int GENERATION_TYPE_ORDERED = 2;
    /**
     * Diagram sites are user-defined.
     * Use with {@link #setGenerationType} and {@code app:generation_type}
     */
    final static public int GENERATION_TYPE_CUSTOM = 3;


    /*
     *   DEFAULTS
     */
    final static private boolean DEF_BORDERS = true;
    final static private int DEF_BORDERS_COLOR = Color.LTGRAY;
    final static private float DEF_BORDER_WIDTH = 3.5f;
    final static private boolean DEF_BORDER_CORNERS_ROUND = true;
    final static private int DEF_GENERATION_TYPE = GENERATION_TYPE_RANDOM;



    /*
     *   FIELDS
     */

    private Paint p;

    private Voronoi mVoronoi;
    private List<VoronoiRegion> mRegions;
    private List<VoronoiRegion.VoronoiPoint> mCustomPoints;
    private OnRegionClickListener mRegionClickListener;

    private int mRegionsCount;
    private int mViewWidth, mViewHeight;
    private int mDistanceBetweenSites = 20;

    /*
     *   ATTRIBUTES
     */
    private boolean mBorderEnabled;
    private int mBorderColor;
    private float mBorderWidth;
    private boolean mRoundCornersEnabled;
    private int mGenerationType;


    /**
     * Interface definition for a callback to be invoked when a Voronoi diagram region is clicked
     */
    public interface OnRegionClickListener {
        /**
         * Callback method to be invoked when a region in this VoronoiView has
         * been clicked.
         *
         * @param view     The region view which was clicked
         * @param position The position of the region
         */
        void onClick(View view, int position);
    }


    /*
     *
     * Constructors/Initialization
     *
     */

    public VoronoiView(Context context) {
        super(context);
        init(null);
    }

    public VoronoiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public VoronoiView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.VoronoiView, 0, 0);
        mBorderEnabled = a.getBoolean(R.styleable.VoronoiView_show_border, DEF_BORDERS);
        mBorderColor = a.getColor(R.styleable.VoronoiView_border_color, DEF_BORDERS_COLOR);
        mBorderWidth = a.getFloat(R.styleable.VoronoiView_border_width, DEF_BORDER_WIDTH);
        mRoundCornersEnabled = a.getBoolean(R.styleable.VoronoiView_border_round, DEF_BORDER_CORNERS_ROUND);
        mGenerationType = a.getInt(R.styleable.VoronoiView_generation_type, DEF_GENERATION_TYPE);
        a.recycle();

        initPaint();
        initDiagram();
    }

    private void initDiagram() {
        mVoronoi = new Voronoi(mDistanceBetweenSites);
    }

    private void initPaint() {
        p = new Paint();
        p.setAntiAlias(true);
        p.setColor(mBorderColor);
        p.setStrokeWidth(mBorderWidth);
        p.setStyle(Paint.Style.STROKE);

        if (mRoundCornersEnabled) {
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setStrokeCap(Paint.Cap.SQUARE);
            p.setPathEffect(new CornerPathEffect(15));
        }
    }


    /**
     * Register a callback to be invoked when a region in the VoronoiView is clicked
     *
     * @param listener The callback that will be invoked.
     */
    public void setOnRegionClickListener(OnRegionClickListener listener) {
        this.mRegionClickListener = listener;
    }


    /**
     * Set the enabled state for regions borders.
     *
     * @param enable True if borders are enabled, false otherwise.
     */
    public void enableBorders(boolean enable) {
        this.mBorderEnabled = enable;

        requestLayout();
    }

    /**
     * Returns the enabled status for regions borders.
     *
     * @return True if borders are enabled, false otherwise.
     */
    public boolean isBorderEnabled() {
        return this.mBorderEnabled;
    }


    /**
     * Sets the color for diagram borders.
     *
     * @param colorId the color of the borders
     */
    public void setBorderColor(int colorId) {
        this.mBorderColor = colorId;

        initPaint();
        requestLayout();
    }

    /**
     * Return a border color id
     *
     * @return color id
     */
    public int getBorderColor() {
        return this.mBorderColor;
    }


    /**
     * Sets the width value for diagram borders
     *
     * @param width the width of the borders
     */
    public void setBorderWidth(float width) {
        this.mBorderWidth = width;

        initPaint();
        requestLayout();
    }

    /**
     * Return the width of the borders
     *
     * @return width
     */
    public float getBorderWidth() {
        return this.mBorderWidth;
    }


    /**
     * Sets border corners to be round
     *
     * @param enable True if border corners should be round, false otherwise.
     */
    public void enableBorderRoundCorners(boolean enable) {
        this.mRoundCornersEnabled = enable;

        initPaint();
        requestLayout();
    }

    /**
     * Are border corners round
     *
     * @return True if border corners are round, false otherwise.
     */
    public boolean isBorderRoundCornerEnabled() {
        return this.mRoundCornersEnabled;
    }


    /**
     * Sets the type of points generation
     *
     * @param points_type points generation type. One of {@link #GENERATION_TYPE_RANDOM}, {@link #GENERATION_TYPE_ORDERED}, or {@link #GENERATION_TYPE_CUSTOM}.
     */
    public void setGenerationType(int points_type) {
        this.mGenerationType = points_type;
        requestLayout();
    }

    /**
     * Return the generation type
     *
     * @return generation type
     */
    public int getGenerationType() {
        return this.mGenerationType;
    }


    /**
     * Sets the user-defined diagram sites if the generation type ({@link #setGenerationType}) is {@link #GENERATION_TYPE_CUSTOM}
     *
     * @param custom_points user-defined sites
     */
    public void setCustomPoints(List<VoronoiPoint> custom_points) {
        this.mCustomPoints = custom_points;
    }


    /**
     * Regenerate a diagram
     */
    public void refresh() {
        countRegions();
        generateDiagram();
        requestLayout();
    }


    /**
     * Set layer-type HARDWARE or SOFTWARE for the view (true/false).
     *
     * @param enabled True if should be hardware acceleration, false if should be software
     */
    public void setHardwareAccelerationEnabled(boolean enabled) {
        if (enabled)
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        else
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }




    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);
        Log.v("View", "view: " + width + ", " + height);

        mViewWidth = width;
        mViewHeight = height;

        generateDiagram();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.setTag(i);
            VoronoiRegion region = mRegions.get(i);
            child.measure(
                    MeasureSpec.makeMeasureSpec((int) region.width, MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec((int) region.height, MeasureSpec.AT_MOST)
            );
            int curWidth = child.getMeasuredWidth();
            int curHeight = child.getMeasuredHeight();

            //set children into the center of the region rectangle
            child.layout(
                    (int) region.center_rect.x - curWidth / 2,
                    (int) region.center_rect.y - curHeight / 2,
                    (int) region.center_rect.x + curWidth,
                    (int) region.center_rect.y + curHeight
            );
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(width, height);

        countRegions();
    }


    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        Integer index = (Integer) child.getTag();
        VoronoiRegion region = mRegions.get(index);

        // firstly clip and draw children
        canvas.clipPath(region.path, Region.Op.REPLACE);
        boolean result = super.drawChild(canvas, child, drawingTime);

        // then draw borders
        if (mBorderEnabled)
            canvas.drawPath(region.path, p);

        // draw site
//        canvas.drawCircle((float)region.site.x, (float)region.site.y, 10, p);

        return result;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mRegionClickListener != null) {

            if (event.getAction() == MotionEvent.ACTION_UP) {
                for (int i = 0; i < mRegions.size(); i++) {
                    VoronoiRegion region = mRegions.get(i);
                    if (region.contains(event.getX(), event.getY())) {
                        mRegionClickListener.onClick(this, i);
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private void countRegions() {
        mRegionsCount = getChildCount();
    }

    private void generateDiagram() {
        if (mViewWidth == 0 || mViewHeight == 0) {
            return;
        }

        double[] arrayX = new double[mRegionsCount];
        double[] arrayY = new double[mRegionsCount];

        switch (mGenerationType) {
            case GENERATION_TYPE_RANDOM:
                generateRandomPoints(arrayX, arrayY);
                break;
            case GENERATION_TYPE_ORDERED:
                generateOrderedPoints(arrayX, arrayY);
                break;
            case GENERATION_TYPE_CUSTOM:
                generateCustomPoints(arrayX, arrayY);
                break;
            default:
                generateRandomPoints(arrayX, arrayY);
                break;
        }


        mVoronoi.generateVoronoi(arrayX, arrayY, 0, mViewWidth, 0, mViewHeight);
        mRegions = mVoronoi.getRegions();
    }



    /*
     *
     * Points generation methods
     *
     */

    private void generateRandomPoints(double[] arrayX, double[] arrayY) {
        Random rand = new Random();

        int i = 0;
        while (i < mRegionsCount) {
            int x1 = rand.nextInt(mViewWidth + 1);
            int y1 = rand.nextInt(mViewHeight + 1);

            boolean good = true;
            for (int j = 0; j < arrayX.length; j++) {
                double x2 = arrayX[j];
                double y2 = arrayY[j];

                double dist = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
                if (dist < mDistanceBetweenSites)
                    good = false;
            }

            if (good) {
                arrayX[i] = x1;
                arrayY[i] = y1;
                i++;
            }
        }
    }


    private void generateOrderedPoints(double[] arrayX, double[] arrayY) {
        Random rand = new Random();

        // Make a number of columns
        int diff_w = Math.max(mViewWidth / mViewHeight, 1);
        int diff_h = Math.max(mViewHeight / mViewWidth, 1);
        boolean correct = false;
        int temp_col = 0;
        int temp_row = 0;
        while (!correct) {
            temp_col += diff_w;
            temp_row += diff_h;
            if (temp_col * temp_row >= mRegionsCount)
                correct = true;
        }


        // Make a number of rows
        int columns = temp_col;
        int bal = mRegionsCount % columns;
        int rows = mRegionsCount / columns;

        // Region width/height
        int reg_width = mViewWidth / columns;
        int reg_height = mViewHeight / rows;


        // Generate points
        int curX = 0;
        int curY = 0;
        int index = 0;
        for (int i = 0; i < rows; i++) {
            if (i == rows - 1 && bal > 0) {
                columns += bal;
                reg_width = mViewWidth / columns;
            }
            for (int j = 0; j < columns; j++) {
                int x1 = rand.nextInt(reg_width / 2) + curX + reg_width / 4;
                int y1 = rand.nextInt(reg_height / 2) + curY + reg_height / 4;

                arrayX[index] = x1;
                arrayY[index] = y1;
                index++;

                curX += reg_width;
            }
            curX = 0;
            curY += reg_height;
        }
    }

    private void generateCustomPoints(double[] arrayX, double[] arrayY) {

        if (mCustomPoints == null || mCustomPoints.size() != mRegionsCount) {
            throw new RuntimeException("The custom points count is not equals to a regions count");
        }

        for (int i = 0; i < mCustomPoints.size(); i++) {
            VoronoiRegion.VoronoiPoint point = mCustomPoints.get(i);
            arrayX[i] = point.x;
            arrayY[i] = point.y;
        }
    }


}
