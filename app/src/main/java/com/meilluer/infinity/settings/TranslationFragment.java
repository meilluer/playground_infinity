package com.meilluer.infinity.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import javax.inject.Inject;

import com.meilluer.infinity.Infinity;
import com.meilluer.infinity.activities.BaseActivity;
import com.meilluer.infinity.adapters.TranslationFragmentRecyclerViewAdapter;
import com.meilluer.infinity.customtheme.CustomThemeWrapper;
import com.meilluer.infinity.databinding.FragmentTranslationBinding;

public class TranslationFragment extends Fragment {

    @Inject
    CustomThemeWrapper customThemeWrapper;
    private BaseActivity activity;

    public TranslationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentTranslationBinding binding = FragmentTranslationBinding.inflate(inflater, container, false);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        TranslationFragmentRecyclerViewAdapter adapter = new TranslationFragmentRecyclerViewAdapter(activity, customThemeWrapper);
        binding.getRoot().setAdapter(adapter);

        binding.getRoot().setBackgroundColor(customThemeWrapper.getBackgroundColor());

        if (activity.isImmersiveInterface()) {
            ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                    Insets allInsets = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                                    | WindowInsetsCompat.Type.displayCutout()
                    );
                    binding.getRoot().setPadding(allInsets.left, 0, allInsets.right, allInsets.bottom);
                    return WindowInsetsCompat.CONSUMED;
                }
            });
        }

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (BaseActivity) context;
    }
}