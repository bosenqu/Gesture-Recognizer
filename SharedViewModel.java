package ca.uwaterloo.cs349;

import android.content.Context;
import android.graphics.*;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

class PairComparer implements Comparator<Pair<Double, Integer>> {
    public int compare(Pair<Double, Integer> a, Pair<Double, Integer> b) {
        return (int)(a.first - b.first);
    }
}

public class SharedViewModel extends ViewModel implements Serializable{

    public final int N = 128;
    public final float STD_SCALE = 300;
    public final int LIBRARY_IMAGE_SCALE = 300;
    public final int LOOKUP_IMAGE_SCALE = 250;
    public final int LOOKUP_SPACING = 50;
    public final float SCALE_FACTOR = (float) 0.7;
    public final int STROKE_PEN_WIDTH = 8;

    public final String SAVE_FILE_NAME = "saved_gestures";

    private final transient  Paint strokePaint = new Paint();
    private final transient Paint greyBackGroundPaint = new Paint();
    private final transient  Paint blueBackGroundPaint = new Paint();

    private MutableLiveData<Boolean> update;
    private List<Pair<String, List<PointF>>> originalGestures;
    private List<List<PointF>> modifiedGestures;

    public SharedViewModel() {
        super();
        greyBackGroundPaint.setStyle(Paint.Style.FILL);
        greyBackGroundPaint.setColor(Color.LTGRAY);
        blueBackGroundPaint.setStyle(Paint.Style.FILL);
        blueBackGroundPaint.setColor(Color.BLUE);

        strokePaint.setColor(Color.BLACK);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(STROKE_PEN_WIDTH);
        strokePaint.setPathEffect(null);

        originalGestures = new ArrayList<>();
        modifiedGestures = new ArrayList<>();

        update = new MutableLiveData<>();
        update.setValue(false);
    }

    public void load(Context context) {
        try {
            FileInputStream fis = context.openFileInput(SAVE_FILE_NAME);
            InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
            Scanner scanner = new Scanner(inputStreamReader);

            for(int i = 0, n = scanner.nextInt(); i < n; ++i) {
                String name = scanner.next();
                List<PointF> points = new ArrayList<>();
                for(int j = 0, m = scanner.nextInt(); j < m; ++j) {
                    points.add(new PointF(scanner.nextFloat(), scanner.nextFloat()));
                }
                if(!points.isEmpty()) {
                    originalGestures.add(new Pair<>(name, points));
                }
            }

            for(int i = 0, n = scanner.nextInt(); i < n; ++i) {
                List<PointF> points = new ArrayList<>();
                for(int j = 0, m = scanner.nextInt(); j < m; ++j) {
                    points.add(new PointF(scanner.nextFloat(), scanner.nextFloat()));
                }
                if(!points.isEmpty()) {
                    modifiedGestures.add(points);
                }
            }

        } catch (FileNotFoundException e) {
            Log.d("ERROR", e.getMessage());
            originalGestures = new ArrayList<>();
            modifiedGestures = new ArrayList<>();
        } catch (IOException e) {
            Log.d("ERROR", e.getMessage());
            originalGestures = new ArrayList<>();
            modifiedGestures = new ArrayList<>();
        }
    }

    public void save(Context context) {
        String s = originalGestures.size() + "\n";
        for(Pair<String, List<PointF>> pair: originalGestures) {
            s += pair.first + " " + pair.second.size() + "\n";
            for(PointF point : pair.second) {
                s += point.x + " " + point.y + "\n";
            }
        }
        s += modifiedGestures.size() + "\n";
        for(List<PointF> points : modifiedGestures) {
            s += points.size() + "\n";
            for(PointF point : points) {
                s += point.x + " " + point.y + "\n";
            }
        }

        try (FileOutputStream fos = context.openFileOutput(SAVE_FILE_NAME, Context.MODE_PRIVATE)) {
            fos.write(s.getBytes(StandardCharsets.UTF_8));

        } catch (IOException e) {
            Log.d("ERROR", e.getMessage());
            e.printStackTrace();
        }
    }

    public MutableLiveData<Boolean> getUpdate() {
        return update;
    }

    public List<RelativeLayout> getLibraryView(Context context) {
        List<RelativeLayout> result = new ArrayList<>();
        for(int i = 0; i < originalGestures.size(); ++i) {
            LinearLayout temp = new LinearLayout(context);
            temp.setOrientation(LinearLayout.HORIZONTAL);
            temp.setGravity(Gravity.CENTER_VERTICAL);

            ImageView imageView = new ImageView(context);
            TextView textView = new TextView(context);

            imageView.setImageBitmap(getDrawingImage(i, LIBRARY_IMAGE_SCALE, greyBackGroundPaint));
            textView.setText(originalGestures.get(i).first);
            textView.setTextSize(20);
            textView.setTextColor(Color.BLACK);
            textView.setPadding(20, 0, 0, 0);

            temp.setPadding(10, 10, 10, 10);
            temp.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            temp.addView(imageView);
            temp.addView(textView);

            RelativeLayout toadd = new RelativeLayout(context);
            toadd.addView(temp);
            result.add(toadd);
        }
        return result;
    }

    public void add(String name, List<PointF> points) {
        List<PointF> resampledPoints = resample(points);
        rotate(resampledPoints);
        translate(resampledPoints, 0, 0);
        resampledPoints = scale(resampledPoints, STD_SCALE);
        translate(points, 0, 0);
        originalGestures.add(new Pair<>(name, points));
        modifiedGestures.add(resampledPoints);
        update.setValue(!update.getValue());
    }

    public List<LinearLayout> lookUp(List<PointF> points, Context context) {
        List<PointF> resampledPoints = resample(points);
        rotate(resampledPoints);
        translate(resampledPoints, 0, 0);
        resampledPoints = scale(resampledPoints, STD_SCALE);

        List<Pair<Double, Integer>> result = new ArrayList<>();
        int index = 0;
        for (List<PointF> gesture: modifiedGestures) {
            result.add(new Pair<>(distance(gesture, resampledPoints), index));
            ++index;
        }

        Collections.sort(result, new PairComparer());

        List<LinearLayout> rv = new ArrayList<>();
        for(int i = 0; i < Math.min(3, result.size()); ++i) {
            LinearLayout temp = new LinearLayout(context);
            temp.setOrientation(LinearLayout.VERTICAL);

            ImageView imageView = new ImageView(context);
            TextView textView = new TextView(context);

            imageView.setImageBitmap(getDrawingImage(result.get(i).second, LOOKUP_IMAGE_SCALE, i == 0 ? blueBackGroundPaint : greyBackGroundPaint));
            textView.setText(originalGestures.get(result.get(i).second).first);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTextColor(Color.BLACK);

            temp.addView(imageView);
            temp.addView(textView);
            temp.setPadding(0, 0, LOOKUP_SPACING, 0);
            rv.add(temp);
        }

        return rv;
    }

    public void remove(int index) {
        if(index >= 0 && index < originalGestures.size()) {
            originalGestures.remove(index);
            modifiedGestures.remove(index);
            update.setValue(!update.getValue());
        }
    }

    public void update(int index, List<PointF> points) {
        if(index >= 0 && index < originalGestures.size()) {
            String name = originalGestures.get(index).first;
            remove(index);
            List<PointF> resampledPoints = resample(points);
            rotate(resampledPoints);
            translate(resampledPoints, 0, 0);
            resampledPoints = scale(resampledPoints, STD_SCALE);
            translate(points, 0, 0);
            originalGestures.add(index, new Pair<>(name, points));
            modifiedGestures.add(index, resampledPoints);
            update.setValue(!update.getValue());
        }
    }

    private double distance(PointF point1, PointF point2) {
        return Math.sqrt(Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2));
    }

    private double distance(double x, double y, PointF point) {
        return Math.sqrt(Math.pow(x - point.x, 2) + Math.pow(y - point.y, 2));
    }

    private double distance(List<PointF> points1, List<PointF> points2) {
        double distance = 0;
        int n = Math.min(points1.size(), points2.size());
        for(int i = 0; i < n; ++i) {
            distance += Math.sqrt(Math.pow(points1.get(i).x - points2.get(i).x, 2) +
                                  Math.pow(points1.get(i).y - points2.get(i).y, 2));
        }
        return distance / n;
    }

    private double distance(List<PointF> points) {
        double total = 0;
        for(int i = 1; i < points.size(); ++i) {
            total += distance(points.get(i - 1), points.get(i));
        }
        return total;
    }

    private List<PointF> resample(List<PointF> points) {
        List<PointF> result = new ArrayList<>();
        double segLength = distance(points) / (N - 1);

        double x = points.get(0).x;
        double y = points.get(0).y;
        double requireLength = segLength;

        result.add(new PointF((float) x, (float) y));

        for(int i = 1; i < points.size(); ++i) {
            while(distance(x, y, points.get(i)) >= requireLength) {
                double alpha = requireLength / distance(x, y, points.get(i));
                x += alpha * (points.get(i).x - x);
                y += alpha * (points.get(i).y - y);
                result.add(new PointF((float) x, (float)y));
                requireLength = segLength;
            }
            requireLength -= distance(x, y, points.get(i));
            x = points.get(i).x;
            y = points.get(i).y;
        }
        if(result.size() == N - 1) {
            result.add(new PointF(points.get(points.size() - 1).x, points.get(points.size() - 1).y));
        }
        return result;
    }

    private PointF centroid(List<PointF> points) {
        float x_sum = 0;
        float y_sum = 0;
        for(PointF point : points) {
            x_sum += point.x;
            y_sum += point.y;
        }
        return new PointF(x_sum / points.size(), y_sum / points.size());
    }

    private void rotate(List<PointF> points) {
        PointF centroid = centroid(points);
        double theta = Math.atan((centroid.y - points.get(0).y) / (points.get(0).x - centroid.x));

        if(points.get(0).x >= centroid.x) {
            if(points.get(0).y > centroid.y) {
                theta = 2 * Math.PI + theta;
            }
        } else {
            theta = Math.PI + theta;
        }

        for(PointF point : points) {
            float x = point.x;
            float y = point.y;
            point.x = (float) (x * Math.cos(theta) - y * Math.sin(theta));
            point.y = (float) (x * Math.sin(theta) + y * Math.cos(theta));
        }
    }

    private void translate(List<PointF> points, float x, float y) {
        PointF centroid = centroid(points);
        for(PointF point : points) {
            point.x -= centroid.x - x;
            point.y -= centroid.y - y;
        }
    }

    private void translateByMinMax(List<PointF> points, float x, float y) {
        float minx = points.get(0).x;
        float maxx = points.get(0).x;
        float miny = points.get(0).y;
        float maxy = points.get(0).y;

        for(int i = 1; i < points.size(); ++i){
            if(points.get(i).x < minx) {
                minx = points.get(i).x;
            }
            if(points.get(i).y < miny) {
                miny = points.get(i).y;
            }
            if(points.get(i).x > maxx) {
                maxx = points.get(i).x;
            }
            if(points.get(i).y > maxy) {
                maxy = points.get(i).y;
            }
        }

        PointF center = new PointF((maxx + minx) / 2, (maxy + miny) / 2);
        for(PointF point : points) {
            point.x -= center.x - x;
            point.y -= center.y - y;
        }
    }

    private List<PointF> scale(List<PointF> points, float prefScale) {
        float minx = points.get(0).x;
        float maxx = points.get(0).x;
        float miny = points.get(0).y;
        float maxy = points.get(0).y;

        for(int i = 1; i < points.size(); ++i){
            if(points.get(i).x < minx) {
                minx = points.get(i).x;
            }
            if(points.get(i).y < miny) {
                miny = points.get(i).y;
            }
            if(points.get(i).x > maxx) {
                maxx = points.get(i).x;
            }
            if(points.get(i).y > maxy) {
                maxy = points.get(i).y;
            }
        }

        float xdiff = maxx - minx;
        float ydiff = maxy - miny;
        float scale = prefScale / Math.max(xdiff, ydiff);

        List<PointF> result = new ArrayList<>();

        for(PointF point : points) {
            result.add(new PointF(point.x * scale, point.y * scale));
        }

        return result;
    }

    private Bitmap getDrawingImage(int index, int scale, Paint backgroundPaint) {
        Bitmap bitmap = Bitmap.createBitmap(scale, scale, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawRect(0, 0, scale, scale , backgroundPaint);

        List<PointF> points = scale(originalGestures.get(index).second, scale * SCALE_FACTOR);
        translateByMinMax(points, scale / 2, scale / 2);

        Path path = new Path();
        path.moveTo(points.get(0).x, points.get(0).y);
        for(int i = 1; i < points.size(); ++i) {
            path.lineTo(points.get(i).x, points.get(i).y);
        }
        canvas.drawPath(path, strokePaint);

        return bitmap;
    }
}