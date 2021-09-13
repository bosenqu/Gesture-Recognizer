package ca.uwaterloo.cs349;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

public class HomeFragment extends Fragment {

    private SharedViewModel mViewModel;
    Context context;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        context = this.getContext();
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        ViewGroup canvas_view_group = root.findViewById(R.id.canvas_region);
        final ViewGroup top_display = root.findViewById(R.id.top_display);
        final Button resetBtn = root.findViewById(R.id.reset_button);
        final Button recognizeBtn = root.findViewById(R.id.recognize_button);
        final DrawingView drawingView = new DrawingView(this.getContext());
        drawingView.addButton(resetBtn);
        drawingView.addButton(recognizeBtn);
        canvas_view_group.addView(drawingView);

        recognizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                top_display.removeAllViews();
                for(LinearLayout layout : mViewModel.lookUp(drawingView.getPoints(), context)) {
                    top_display.addView(layout);
                }
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                top_display.removeAllViews();
                drawingView.clearView();
            }
        });

        return root;
    }



}