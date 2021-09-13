package ca.uwaterloo.cs349;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class UpdateGesture  {

    private SharedViewModel mViewModel;

    public UpdateGesture(final ViewGroup parent, final ViewGroup update, SharedViewModel viewModel, Context context, final int index) {
        this.mViewModel = viewModel;

        final Button okBtn = new Button(context);
        okBtn.setText("OK");
        final Button clearBtn = new Button(context);
        clearBtn.setText("CLEAR");
        final Button backBtn = new Button(context);
        backBtn.setText("BACK");
        final DrawingView drawingView = new DrawingView(context);

        update.removeAllViews();
        parent.removeAllViews();
        parent.getLayoutParams().height = LinearLayout.LayoutParams.MATCH_PARENT;

        LinearLayout topButton = new LinearLayout(context);
        topButton.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout canvas_view_group = new LinearLayout(context);
        canvas_view_group.setOrientation(LinearLayout.VERTICAL);

        drawingView.addButton(okBtn);
        drawingView.addButton(clearBtn);
        topButton.addView(okBtn);
        topButton.addView(clearBtn);
        topButton.addView(backBtn);

        parent.addView(topButton);
        parent.addView(canvas_view_group);
        canvas_view_group.addView(drawingView);

        canvas_view_group.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
        canvas_view_group.getLayoutParams().height = LinearLayout.LayoutParams.MATCH_PARENT;

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.clearView();
            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update.getLayoutParams().height = LinearLayout.LayoutParams.MATCH_PARENT;
                parent.removeAllViews();
                mViewModel.update(index, drawingView.getPoints());
                drawingView.clearView();
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update.getLayoutParams().height = LinearLayout.LayoutParams.MATCH_PARENT;
                parent.removeAllViews();
                mViewModel.getUpdate().setValue(!mViewModel.getUpdate().getValue());
            }
        });
    }
}
