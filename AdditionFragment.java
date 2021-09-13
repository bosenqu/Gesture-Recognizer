package ca.uwaterloo.cs349;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PointF;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


public class AdditionFragment extends Fragment {

    private SharedViewModel mViewModel;
    private Context context;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        context = this.getContext();

        final View root = inflater.inflate(R.layout.fragment_addition, container, false);
        final ViewGroup canvas_view_group = root.findViewById(R.id.canvas_region);
        final Button okBtn = root.findViewById(R.id.ok_button);
        final Button clearBtn = root.findViewById(R.id.clear_button);
        final DrawingView drawingView = new DrawingView(this.getContext());

        drawingView.addButton(okBtn);
        drawingView.addButton(clearBtn);
        canvas_view_group.addView(drawingView);

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.clearView();
            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder nameInputBuilder = new AlertDialog.Builder(context);
                nameInputBuilder.setTitle("Please enter a name for this gesture");
                final EditText input = new EditText(context);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                nameInputBuilder.setView(input);

                nameInputBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mViewModel.add(input.getText().toString(), drawingView.getPoints());
                        drawingView.clearView();
                    }
                });

                nameInputBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                nameInputBuilder.show();
            }
        });
        return root;
    }


}