package ml.docilealligator.infinityforreddit.utils;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;
import android.util.Log;

import android.widget.PopupWindow;
import android.util.Log;



import java.util.ArrayList;
import java.util.List;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.adapters.SuggestionAdapter;
import ml.docilealligator.infinityforreddit.tasks.FetchListOfSubredditsTask;
import ml.docilealligator.infinityforreddit.tasks.FetchListOfUsersTask;

public class RedditMentionHelper {
    private final Activity activity;
    private final EditText editText;

    private PopupWindow dialog;
    private ListView listView;
    private SuggestionAdapter adapter;
    private List<Suggestion> items = new ArrayList<>();

    public RedditMentionHelper(Activity activity, EditText editText) {
        this.activity = activity;
        this.editText = editText;
    }

    public void setup() {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                int cursorPosition = editText.getSelectionStart();

                if (cursorPosition > 0) {
                    int atIndex = text.lastIndexOf("r/", cursorPosition - 1);
                    if (atIndex == -1) {
                        atIndex = text.lastIndexOf("u/", cursorPosition - 1);
                    }

                    if (atIndex != -1) {
                        String prefix = text.substring(atIndex, atIndex + 2);
                        String query = text.substring(atIndex + 2, cursorPosition);

                        if (prefix.equals("r/")) {
                            if (query.length() > 0) {
                                searchSubreddits(query);
                            } else {
                                hideSuggestions();
                            }
                        } else if (prefix.equals("u/")) {
                            if (query.length() > 0) {
                                searchUsers(query);
                            } else {
                                hideSuggestions();
                            }
                        }
                    } else {
                        hideSuggestions();
                    }
                } else {
                    hideSuggestions();
                }
            }
        });
    }

    private void searchSubreddits(String query) {
        new FetchListOfSubredditsTask(activity, subreddits -> {
            items.clear();
            items.addAll(subreddits);
            showSuggestions("r/");
        }).execute(query);
    }

    private void searchUsers(String query) {
        new FetchListOfUsersTask(activity, users -> {
            items.clear();
            items.addAll(users);
            showSuggestions("u/");
        }).execute(query);
    }

    private void showSuggestions(String prefix) {
        Log.d("RedditMentionHelper", "showSuggestions, items size: " + items.size());
        if (items.isEmpty()) {
            hideSuggestions();
            return;
        }

        if (dialog == null) {
            listView = new ListView(activity);
            adapter = new SuggestionAdapter(activity, items);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener((parent, view, position, id) -> {
                Suggestion selectedItem = items.get(position);
                String text = editText.getText().toString();
                int cursorPosition = editText.getSelectionStart();
                int atIndex = text.lastIndexOf(prefix, cursorPosition - 1);

                if (atIndex != -1) {
                    String newText = text.substring(0, atIndex + prefix.length()) + selectedItem.getName() + text.substring(cursorPosition);
                    editText.setText(newText);
                    editText.setSelection(atIndex + prefix.length() + selectedItem.getName().length());
                }
                hideSuggestions();
            });

            dialog = new PopupWindow(activity);
            dialog.setContentView(listView);
            dialog.setFocusable(false);
            dialog.setOutsideTouchable(true);
            dialog.setWidth(editText.getWidth());
        } else {
            adapter.notifyDataSetChanged();
        }

        if (!dialog.isShowing()) {
            Log.d("RedditMentionHelper", "Showing dropdown");
            editText.post(() -> dialog.showAsDropDown(editText));
        }
    }

    private void hideSuggestions() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
