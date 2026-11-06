package quatja.com.vorolay;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
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
    private List<VoronoiRegion> mRegions = Collections.emptyList();
    private final List<View> mVisibleChildren = new ArrayList<>();
    private final List<View> mLayoutChildren = new ArrayList<>();
    private boolean mDiagramDirty = true;
    private List<VoronoiRegion.VoronoiPoint> mCustomPoints;
    private boolean mCustomPointsRelative;
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
         * @param position The child index in this ViewGroup
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
        int customPointsResource = a.getResourceId(R.styleable.VoronoiView_custom_points, 0);
        a.recycle();

        if (customPointsResource != 0) {
            mCustomPoints = parseRelativePoints(getResources().getStringArray(customPointsResource));
            mCustomPointsRelative = true;
        }

        initPaint();
        initDiagram();
    }

    private void initDiagram() {
        mVoronoi = new Voronoi(0);
    }

    private static List<VoronoiPoint> parseRelativePoints(String[] entries) {
        List<VoronoiPoint> points = new ArrayList<>(entries.length);
        for (int i = 0; i < entries.length; i++) {
            String[] coordinates = entries[i].split(",", -1);
            if (coordinates.length != 2) {
                throw new IllegalArgumentException("custom_points entry " + i + " must contain x, y");
            }
            double x;
            double y;
            try {
                x = Double.parseDouble(coordinates[0].trim());
                y = Double.parseDouble(coordinates[1].trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("custom_points entry " + i + " must contain numeric x, y", e);
            }
            if (Double.isNaN(x) || Double.isNaN(y) || x < 0 || x > 1 || y < 0 || y > 1) {
                throw new IllegalArgumentException("custom_points entry " + i + " coordinates must be between 0 and 1");
            }
            points.add(new VoronoiPoint(x, y));
        }
        return points;
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

        invalidate();
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
        invalidate();
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
        invalidate();
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
        invalidate();
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
        refresh();
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
     * Coordinates are pixels relative to the content area. This replaces any points supplied in XML.
     *
     * @param custom_points user-defined sites
     */
    public void setCustomPoints(List<VoronoiPoint> custom_points) {
        this.mCustomPoints = custom_points == null ? null : new ArrayList<>(custom_points);
        this.mCustomPointsRelative = false;
        refresh();
    }


    /**
     * Regenerate a diagram
     */
    public void refresh() {
        mDiagramDirty = true;
        requestLayout();
        invalidate();
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
        mDiagramDirty = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getSuggestedMinimumWidth();
        int height = getSuggestedMinimumHeight();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            width = Math.max(width, child.getMeasuredWidth() + getPaddingLeft() + getPaddingRight());
            height = Math.max(height, child.getMeasuredHeight() + getPaddingTop() + getPaddingBottom());
        }
        setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        List<View> visible = mLayoutChildren;
        visible.clear();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) visible.add(child);
        }
        int width = Math.max(0, getWidth() - getPaddingLeft() - getPaddingRight());
        int height = Math.max(0, getHeight() - getPaddingTop() - getPaddingBottom());
        if (!visible.equals(mVisibleChildren) || width != mViewWidth || height != mViewHeight) {
            mDiagramDirty = true;
        }
        mVisibleChildren.clear();
        mVisibleChildren.addAll(visible);
        mRegionsCount = visible.size();
        mViewWidth = width;
        mViewHeight = height;
        if (mDiagramDirty) {
            generateDiagram();
            mDiagramDirty = false;
        }
        for (int i = 0; i < mRegions.size(); i++) {
            View child = visible.get(i);
            VoronoiRegion region = mRegions.get(i);
            int availableWidth = Math.max(0, (int) region.width);
            int availableHeight = Math.max(0, (int) region.height);
            LayoutParams params = child.getLayoutParams();
            child.measure(getChildMeasureSpec(MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.EXACTLY), 0, params.width),
                    getChildMeasureSpec(MeasureSpec.makeMeasureSpec(availableHeight, MeasureSpec.EXACTLY), 0, params.height));
            int left = getPaddingLeft() + (int) region.center_rect.x - child.getMeasuredWidth() / 2;
            int top = getPaddingTop() + (int) region.center_rect.y - child.getMeasuredHeight() / 2;
            child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        int index = mVisibleChildren.indexOf(child);
        if (index < 0 || index >= mRegions.size()) return false;
        VoronoiRegion region = mRegions.get(index);
        int save = canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());
        canvas.clipPath(region.path);
        canvas.translate(-getPaddingLeft(), -getPaddingTop());
        boolean result = super.drawChild(canvas, child, drawingTime);
        canvas.translate(getPaddingLeft(), getPaddingTop());
        if (mBorderEnabled) canvas.drawPath(region.path, p);
        canvas.restoreToCount(save);
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mRegionClickListener == null) return super.onTouchEvent(event);
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            float x = event.getX() - getPaddingLeft();
            float y = event.getY() - getPaddingTop();
            for (int i = 0; i < mRegions.size(); i++) {
                if (mRegions.get(i).contains(x, y)) {
                    performClick();
                    View child = mVisibleChildren.get(i);
                    mRegionClickListener.onClick(child, indexOfChild(child));
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void generateDiagram() {
        if (mViewWidth == 0 || mViewHeight == 0 || mRegionsCount == 0) {
            mRegions = Collections.emptyList();
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
        Random random = new Random();
        for (int i = 0; i < mRegionsCount; i++) {
            boolean found = false;
            for (int attempt = 0; attempt < 100; attempt++) {
                double x = random.nextDouble() * mViewWidth;
                double y = random.nextDouble() * mViewHeight;
                boolean valid = true;
                for (int j = 0; j < i; j++) {
                    if (Math.hypot(x - arrayX[j], y - arrayY[j]) < mDistanceBetweenSites) {
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    arrayX[i] = x;
                    arrayY[i] = y;
                    found = true;
                    break;
                }
            }
            if (!found) {
                generateOrderedPoints(arrayX, arrayY);
                return;
            }
        }
    }

    private void generateOrderedPoints(double[] arrayX, double[] arrayY) {
        int columns = Math.max(1, (int) Math.ceil(Math.sqrt((double) mRegionsCount * mViewWidth / mViewHeight)));
        columns = Math.min(columns, mRegionsCount);
        int rows = (mRegionsCount + columns - 1) / columns;
        for (int i = 0; i < mRegionsCount; i++) {
            arrayX[i] = (i % columns + 0.5) * mViewWidth / columns;
            arrayY[i] = (i / columns + 0.5) * mViewHeight / rows;
        }
    }

    private void generateCustomPoints(double[] arrayX, double[] arrayY) {

        if (mCustomPoints == null || mCustomPoints.size() != mRegionsCount) {
            throw new IllegalArgumentException("Custom point count must match the number of visible children");
        }

        for (int i = 0; i < mCustomPoints.size(); i++) {
            VoronoiRegion.VoronoiPoint point = mCustomPoints.get(i);
            if (point == null) {
                throw new IllegalArgumentException("Custom points must be inside the content bounds");
            }
            double x = mCustomPointsRelative ? point.x * mViewWidth : point.x;
            double y = mCustomPointsRelative ? point.y * mViewHeight : point.y;
            if (Double.isNaN(x) || Double.isNaN(y)
                    || x < 0 || x > mViewWidth || y < 0 || y > mViewHeight) {
                throw new IllegalArgumentException("Custom points must be inside the content bounds");
            }
            for (int j = 0; j < i; j++) {
                if (arrayX[j] == x && arrayY[j] == y) {
                    throw new IllegalArgumentException("Custom points must be distinct");
                }
            }
            arrayX[i] = x;
            arrayY[i] = y;
        }
    }


}
