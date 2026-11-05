package quatja.com.vorolay;

import static org.junit.Assert.*;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Xml;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.Arrays;
import org.xmlpull.v1.XmlPullParser;
import quatja.com.vorolay.diagram.VoronoiRegion.VoronoiPoint;

@RunWith(AndroidJUnit4.class)
public class VoronoiViewTest {
    private void layout(VoronoiView view) {
        int spec = View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.EXACTLY);
        view.measure(spec, spec);
        view.layout(0, 0, 200, 200);
    }

    @Test public void layoutKeepsTagsAndUpdatesChildren() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            VoronoiView view = new VoronoiView(InstrumentationRegistry.getInstrumentation().getTargetContext());
            view.setGenerationType(VoronoiView.GENERATION_TYPE_ORDERED);
            View first = new View(view.getContext());
            first.setTag("caller tag");
            View second = new View(view.getContext());
            view.addView(first, new ViewGroup.LayoutParams(40, 30));
            view.addView(second, new ViewGroup.LayoutParams(40, 30));
            layout(view);
            assertEquals("caller tag", first.getTag());
            assertEquals(40, first.getWidth());
            assertEquals(30, first.getHeight());
            second.setVisibility(View.GONE);
            layout(view);
            view.draw(new Canvas(Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)));
            view.removeView(first);
            layout(view);
        });
    }

    @Test public void customPointsFollowChildOrder() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            VoronoiView view = new VoronoiView(InstrumentationRegistry.getInstrumentation().getTargetContext());
            View lower = new View(view.getContext());
            View upper = new View(view.getContext());
            view.addView(lower, new ViewGroup.LayoutParams(20, 20));
            view.addView(upper, new ViewGroup.LayoutParams(20, 20));
            view.setCustomPoints(Arrays.asList(new VoronoiPoint(100, 160), new VoronoiPoint(100, 40)));
            view.setGenerationType(VoronoiView.GENERATION_TYPE_CUSTOM);
            layout(view);
            assertTrue(lower.getTop() > upper.getTop());
        });
    }

    @Test public void customRegionsMatchNearestPointAndRefresh() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            VoronoiView view = new VoronoiView(InstrumentationRegistry.getInstrumentation().getTargetContext());
            view.setPadding(10, 10, 10, 10);
            view.enableBorders(false);
            int[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW};
            for (int color : colors) {
                View child = new View(view.getContext());
                child.setBackgroundColor(color);
                view.addView(child, new ViewGroup.LayoutParams(-1, -1));
            }
            VoronoiPoint[] points = {new VoronoiPoint(145, 140), new VoronoiPoint(30, 25),
                    new VoronoiPoint(145, 30), new VoronoiPoint(30, 145)};
            view.setCustomPoints(Arrays.asList(points));
            view.setGenerationType(VoronoiView.GENERATION_TYPE_CUSTOM);
            assertRegions(view, points, colors);
            VoronoiPoint swap = points[0];
            points[0] = points[1];
            points[1] = swap;
            view.setCustomPoints(Arrays.asList(points));
            assertRegions(view, points, colors);
            points[0] = new VoronoiPoint(140, 155);
            points[1] = new VoronoiPoint(32, 18);
            points[2] = new VoronoiPoint(95, 65);
            points[3] = new VoronoiPoint(25, 145);
            view.setCustomPoints(Arrays.asList(points));
            assertRegions(view, points, colors);
        });
    }

    @Test public void regionClickReportsOriginalChildIndex() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            VoronoiView view = new VoronoiView(InstrumentationRegistry.getInstrumentation().getTargetContext());
            View hidden = new View(view.getContext());
            hidden.setVisibility(View.GONE);
            view.addView(hidden);
            View child = new View(view.getContext());
            view.addView(child);
            view.setCustomPoints(Arrays.asList(new VoronoiPoint(100, 100)));
            view.setGenerationType(VoronoiView.GENERATION_TYPE_CUSTOM);
            layout(view);
            int[] clickedIndex = {-1};
            view.setOnRegionClickListener((clicked, position) -> {
                assertSame(child, clicked);
                clickedIndex[0] = position;
            });
            MotionEvent down = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 100, 100, 0);
            MotionEvent up = MotionEvent.obtain(0, 1, MotionEvent.ACTION_UP, 100, 100, 0);
            try {
                assertTrue(view.onTouchEvent(down));
                assertTrue(view.onTouchEvent(up));
                assertEquals(1, clickedIndex[0]);
            } finally {
                down.recycle();
                up.recycle();
            }
        });
    }

    @Test public void invalidCustomPointsAreRejected() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            VoronoiView view = new VoronoiView(InstrumentationRegistry.getInstrumentation().getTargetContext());
            view.addView(new View(view.getContext()));
            view.addView(new View(view.getContext()));
            view.setGenerationType(VoronoiView.GENERATION_TYPE_CUSTOM);
            view.setCustomPoints(Arrays.asList(new VoronoiPoint(10, 10), new VoronoiPoint(10, 10)));
            try {
                layout(view);
                fail("Duplicate points must be rejected");
            } catch (IllegalArgumentException expected) {
                assertTrue(expected.getMessage().contains("distinct"));
            }
        });
    }

    @Test public void xmlPointsScaleWithContentAndCanBeReplaced() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            VoronoiView view = xmlView("test_custom_points");
            view.setPadding(20, 10, 20, 10);
            View gone = new View(view.getContext());
            gone.setVisibility(View.GONE);
            view.addView(gone);
            View first = new View(view.getContext());
            first.setVisibility(View.INVISIBLE);
            view.addView(first);
            view.addView(new View(view.getContext()));
            int[] clicked = {-1};
            view.setOnRegionClickListener((child, index) -> clicked[0] = index);
            layoutAtSize(view, 200, 200);
            click(view, 150, 100);
            assertEquals(2, clicked[0]);
            layoutAtSize(view, 400, 200);
            click(view, 150, 100);
            assertEquals(1, clicked[0]);

            view.setCustomPoints(Arrays.asList(new VoronoiPoint(300, 90), new VoronoiPoint(60, 90)));
            layoutAtSize(view, 400, 200);
            click(view, 80, 100);
            assertEquals(2, clicked[0]);
        });
    }

    @Test public void xmlPointsRejectInvalidValuesAndLayoutErrors() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            for (String name : new String[]{"test_invalid_custom_points", "test_out_of_range_custom_points"}) {
                try {
                    xmlView(name);
                    fail("Invalid XML points must be rejected: " + name);
                } catch (IllegalArgumentException expected) {
                    assertTrue(expected.getMessage().contains("custom_points entry 0"));
                }
            }
            VoronoiView duplicate = xmlView("test_duplicate_custom_points");
            duplicate.addView(new View(duplicate.getContext()));
            duplicate.addView(new View(duplicate.getContext()));
            try {
                layout(duplicate);
                fail("Duplicate XML points must be rejected");
            } catch (IllegalArgumentException expected) {
                assertTrue(expected.getMessage().contains("distinct"));
            }
            VoronoiView mismatch = xmlView("test_custom_points");
            mismatch.addView(new View(mismatch.getContext()));
            try {
                layout(mismatch);
                fail("A point count mismatch must be rejected");
            } catch (IllegalArgumentException expected) {
                assertTrue(expected.getMessage().contains("count"));
            }
        });
    }

    private VoronoiView xmlView(String name) {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        int id = context.getResources().getIdentifier(name, "layout", context.getPackageName());
        assertNotEquals(0, id);
        try (XmlResourceParser parser = context.getResources().getLayout(id)) {
            while (parser.next() != XmlPullParser.START_TAG) { }
            return new VoronoiView(context, Xml.asAttributeSet(parser));
        } catch (java.io.IOException | org.xmlpull.v1.XmlPullParserException e) {
            throw new AssertionError(e);
        }
    }

    private void layoutAtSize(VoronoiView view, int width, int height) {
        view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        view.layout(0, 0, width, height);
    }

    private void click(VoronoiView view, float x, float y) {
        MotionEvent event = MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, x, y, 0);
        try {
            view.onTouchEvent(event);
        } finally {
            event.recycle();
        }
    }

    private void assertRegions(VoronoiView view, VoronoiPoint[] points, int[] colors) {
        layout(view);
        Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(bitmap));
        for (int y = 20; y < 180; y += 20) {
            for (int x = 20; x < 180; x += 20) {
                int nearest = 0;
                double best = Double.MAX_VALUE;
                double second = Double.MAX_VALUE;
                for (int i = 0; i < points.length; i++) {
                    double dx = x - 10 - points[i].x;
                    double dy = y - 10 - points[i].y;
                    double distance = dx * dx + dy * dy;
                    if (distance < best) { second = best; best = distance; nearest = i; }
                    else if (distance < second) { second = distance; }
                }
                if (second - best > 500) {
                    assertEquals("wrong region at " + x + "," + y, colors[nearest], bitmap.getPixel(x, y));
                }
            }
        }
    }
}
