package ca.uwaterloo.cs349;

import android.content.Context;
import android.graphics.*;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {

    private Paint paint = new Paint();

    private List<PointF> points = null;

    private List<Button> buttons = new ArrayList<>();

    public DrawingView(Context context) {
        super(context);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(12);
        paint.setPathEffect(null);
    }

    public void addButton(Button button) {
        button.setEnabled(false);
        buttons.add(button);
    }

    public List<PointF> getPoints() {
        return points;
    }

    public void clearView() {
        points = null;
        invalidate();
        for(Button button : buttons) {
            button.setEnabled(false);
        }
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        super.setOnTouchListener(l);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                points.add(new PointF(event.getX(), event.getY()));
                invalidate();
                return true;
            case MotionEvent.ACTION_DOWN:
                points = new ArrayList<>();
                points.add(new PointF(event.getX(), event.getY()));
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                points.add(new PointF(event.getX(), event.getY()));
                invalidate();
                for(Button button : buttons) {
                    button.setEnabled(true);
                }
                return true;
        }

        return false;
    }

    public void setPoints(List<PointF> points) {
        this.points = points;
        invalidate();
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(points == null) {
            return;
        }
        Path path = new Path();
        path.moveTo(points.get(0).x, points.get(0).y);
        for(int i = 1; i < points.size(); ++i) {
            path.lineTo(points.get(i).x, points.get(i).y);
        }
        canvas.drawPath(path, paint);
        /*
        Paint pointPaint = new Paint();
        pointPaint.setColor(Color.RED);
        for(PointF point : points) {
            canvas.drawCircle(point.x, point.y, 5, pointPaint);
        }*/
    }
}
