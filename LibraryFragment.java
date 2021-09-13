package ca.uwaterloo.cs349;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

public class LibraryFragment extends Fragment {

    public final int BUTTON_SIZE = 100;

    private SharedViewModel mViewModel;
    private ViewGroup altView;
    private ViewGroup mainViewGroup;
    private Context context;
    private View root;

    private int currentIndex = -1;
    private RelativeLayout currentLayout = null;

    private LinearLayout control;
    private ImageView deleteBtn;
    private ImageView updateBtn;

    public View onCreateView(@NonNull final LayoutInflater inflater,
                             final ViewGroup container, Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        context = this.getContext();
        root = inflater.inflate(R.layout.fragment_library, container, false);
        altView = root.findViewById(R.id.alt_view);
        mainViewGroup = root.findViewById(R.id.main_region);

        deleteBtn = new ImageView(context);
        deleteBtn.setImageResource(R.mipmap.delete);
        updateBtn = new ImageView(context);
        updateBtn.setImageResource(R.mipmap.update);
        control = new LinearLayout(context);
        control.addView(deleteBtn);
        control.addView(updateBtn);
        deleteBtn.getLayoutParams().height = BUTTON_SIZE;
        deleteBtn.getLayoutParams().width = BUTTON_SIZE;
        updateBtn.getLayoutParams().height = BUTTON_SIZE;
        updateBtn.getLayoutParams().width = BUTTON_SIZE;

        mViewModel.getUpdate().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                mainViewGroup.removeAllViews();
                int index = 0;
                for(final RelativeLayout layout : mViewModel.getLibraryView(context)) {
                    final int finalIndex = index;
                    layout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            updateCurrentSelection(layout, finalIndex);
                        }
                    });
                    ++index;
                    mainViewGroup.addView(layout);
                }
            }
        });

        mainViewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCurrentSelection(null, -1);
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateGesture update = new UpdateGesture(altView, mainViewGroup, mViewModel, context, currentIndex);
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.remove(currentIndex);
            }
        });
        return root;
    }

    private void updateCurrentSelection(RelativeLayout layout, final int index) {
        if(currentLayout != null) {
            currentLayout.removeViewAt(currentLayout.getChildCount() - 1);
        }
        currentLayout = layout;
        currentIndex = index;
        if(currentLayout != null) {
            currentLayout.addView(control);
            control.setPadding(0, 0, 50, 0);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)control.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            control.setLayoutParams(params);
        }
    }
}