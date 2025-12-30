package ml.docilealligator.infinityforreddit.customviews.preference;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceViewHolder;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.credit_remaining;
import ml.docilealligator.infinityforreddit.utils.APIUtils;

public class ElevenLabsApiKeyPreference extends CustomFontPreference {
    private static final String KEY_SELECTED = "elevenlabs_api_key";
    private static final String KEY_LIST = "elevenlabs_api_keys_list";

    public ElevenLabsApiKeyPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ElevenLabsApiKeyPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ElevenLabsApiKeyPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ElevenLabsApiKeyPreference(Context context) {
        super(context);
    }

    @Override
    public void onAttached() {
        super.onAttached();
        String selectedKey = getSharedPreferences().getString(KEY_SELECTED, "");
        setSummary(selectedKey.isEmpty() ? "No key selected" : selectedKey);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        View infoButton = holder.findViewById(R.id.info_button);
        if (infoButton != null) {
            if (customThemeWrapper != null && infoButton instanceof ImageView) {
                ((ImageView) infoButton).setColorFilter(customThemeWrapper.getPrimaryIconColor(), android.graphics.PorterDuff.Mode.SRC_IN);
            }
            infoButton.setOnClickListener(v -> {
                new credit_remaining().apicheck(getContext());
            });
        }
    }

    @Override
    protected void onClick() {
        showManageKeysDialog();
    }

    private void showManageKeysDialog() {
        Set<String> keysSet = getSharedPreferences().getStringSet(KEY_LIST, new HashSet<>());
        List<String> keysList = new ArrayList<>(keysSet);
        String selectedKey = getSharedPreferences().getString(KEY_SELECTED, "");

        int selectedIndex = -1;
        String[] keysArray = new String[keysList.size()];
        for (int i = 0; i < keysList.size(); i++) {
            keysArray[i] = keysList.get(i);
            if (keysArray[i].equals(selectedKey)) {
                selectedIndex = i;
            }
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialogTheme);
        builder.setTitle("Manage ElevenLabs API Keys");

        if (keysList.isEmpty()) {
            builder.setMessage("No API keys saved. Click ADD to add one.");
        } else {
            builder.setSingleChoiceItems(keysArray, selectedIndex, (dialog, which) -> {
                String key = keysArray[which];
                setSelectedKey(key);
                dialog.dismiss();
            });
        }

        builder.setPositiveButton("Add", (dialog, which) -> showAddKeyDialog());

        if (!keysList.isEmpty()) {
            builder.setNeutralButton("Delete", (dialog, which) -> {
                showDeleteKeysDialog(keysList);
            });
        }

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showAddKeyDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialogTheme);
        builder.setTitle("Add ElevenLabs API Key");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        if (typeface != null) {
            input.setTypeface(typeface);
        }
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newKey = input.getText().toString().trim();
            if (!newKey.isEmpty()) {
                Set<String> keysSet = new HashSet<>(getSharedPreferences().getStringSet(KEY_LIST, new HashSet<>()));
                keysSet.add(newKey);
                getSharedPreferences().edit().putStringSet(KEY_LIST, keysSet).apply();

                if (getSharedPreferences().getString(KEY_SELECTED, "").isEmpty()) {
                    setSelectedKey(newKey);
                }

                showManageKeysDialog();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> showManageKeysDialog());
        builder.show();
    }

    private void showDeleteKeysDialog(List<String> keysList) {
        String[] keysArray = keysList.toArray(new String[0]);
        boolean[] checkedItems = new boolean[keysList.size()];

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialogTheme);
        builder.setTitle("Select Keys to Delete");
        builder.setMultiChoiceItems(keysArray, checkedItems, (dialog, which, isChecked) -> checkedItems[which] = isChecked);

        builder.setPositiveButton("Delete", (dialog, which) -> {
            Set<String> keysSet = new HashSet<>(getSharedPreferences().getStringSet(KEY_LIST, new HashSet<>()));
            String currentSelected = getSharedPreferences().getString(KEY_SELECTED, "");
            boolean selectedDeleted = false;

            for (int i = 0; i < checkedItems.length; i++) {
                if (checkedItems[i]) {
                    keysSet.remove(keysArray[i]);
                    if (keysArray[i].equals(currentSelected)) {
                        selectedDeleted = true;
                    }
                }
            }

            getSharedPreferences().edit().putStringSet(KEY_LIST, keysSet).apply();

            if (selectedDeleted) {
                if (keysSet.isEmpty()) {
                    setSelectedKey("");
                } else {
                    setSelectedKey(keysSet.iterator().next());
                }
            }

            showManageKeysDialog();
        });

        builder.setNegativeButton("Back", (dialog, which) -> showManageKeysDialog());
        builder.show();
    }

    private void setSelectedKey(String key) {
        getSharedPreferences().edit().putString(KEY_SELECTED, key).apply();
        APIUtils.Elevenlabs = key;
        setSummary(key.isEmpty() ? "No key selected" : key);
        notifyChanged();
    }
}