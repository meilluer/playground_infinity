package ml.docilealligator.infinityforreddit.customviews.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.preference.PreferenceViewHolder;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import ml.docilealligator.infinityforreddit.R;

public class CustomFontHelpEditTextPreference extends CustomFontEditTextPreference {

    public CustomFontHelpEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public CustomFontHelpEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CustomFontHelpEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomFontHelpEditTextPreference(Context context) {
        super(context);
        init();
    }

    private void init() {
        setWidgetLayoutResource(R.layout.preference_info_widget);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View infoIcon = holder.findViewById(R.id.info_icon);
        if (infoIcon instanceof ImageView) {
            if (customThemeWrapper != null) {
                ((ImageView) infoIcon).setColorFilter(customThemeWrapper.getPrimaryIconColor(), android.graphics.PorterDuff.Mode.SRC_IN);
            }
            infoIcon.setOnClickListener(v -> showHelpDialog(getContext()));
        }
    }

    private void showHelpDialog(Context context) {
        String key = getKey();
        int explanationResId = 0;
        int exampleResId = 0;

        if ("reddit_api_key".equals(key)) {
            explanationResId = R.string.api_help_reddit_api_key_explanation;
            exampleResId = R.string.api_help_reddit_api_key_example;
        } else if ("anonymous_client_id".equals(key)) {
            explanationResId = R.string.api_help_anonymous_client_id_explanation;
            exampleResId = R.string.api_help_anonymous_client_id_example;
        } else if ("giphy_api_key".equals(key)) {
            explanationResId = R.string.api_help_giphy_api_key_explanation;
            exampleResId = R.string.api_help_giphy_api_key_example;
        } else if ("user_agent".equals(key)) {
            explanationResId = R.string.api_help_user_agent_explanation;
            exampleResId = R.string.api_help_user_agent_example;
        } else if ("gemini_key".equals(key)) {
            explanationResId = R.string.api_help_gemini_key_explanation;
            exampleResId = R.string.api_help_gemini_key_example;
        } else if ("redirect_uri".equals(key)) {
            explanationResId = R.string.api_help_redirect_uri_explanation;
            exampleResId = R.string.api_help_redirect_uri_example;
        }

        if (explanationResId == 0 || exampleResId == 0) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_api_key_help, null);

        TextView explanationLabel = dialogView.findViewById(R.id.explanation_label);
        TextView explanationValue = dialogView.findViewById(R.id.explanation_value);
        TextView exampleLabel = dialogView.findViewById(R.id.example_label);
        TextView exampleValue = dialogView.findViewById(R.id.example_value);

        explanationValue.setText(explanationResId);
        exampleValue.setText(exampleResId);

        if (customThemeWrapper != null) {
            int primaryTextColor = customThemeWrapper.getPrimaryTextColor();
            int secondaryTextColor = customThemeWrapper.getSecondaryTextColor();

            explanationLabel.setTextColor(primaryTextColor);
            explanationValue.setTextColor(secondaryTextColor);
            exampleLabel.setTextColor(primaryTextColor);
            exampleValue.setTextColor(secondaryTextColor);
        }

        if (typeface != null) {
            explanationLabel.setTypeface(typeface);
            explanationValue.setTypeface(typeface);
            exampleLabel.setTypeface(typeface);
            exampleValue.setTypeface(typeface);
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.MaterialAlertDialogTheme)
                .setTitle(getTitle())
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, null);

        builder.show();
    }
}
