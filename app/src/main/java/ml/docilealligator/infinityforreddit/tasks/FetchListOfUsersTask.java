package ml.docilealligator.infinityforreddit.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.Suggestion;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FetchListOfUsersTask extends AsyncTask<String, Void, List<Suggestion>> {
    private final Activity activity;
    private final OnUsersFetchedListener listener;

    public interface OnUsersFetchedListener {
        void onUsersFetched(List<Suggestion> users);
    }

    public FetchListOfUsersTask(Activity activity, OnUsersFetchedListener listener) {
        this.activity = activity;
        this.listener = listener;
    }

    @Override
    protected List<Suggestion> doInBackground(String... params) {
        String query = params[0];
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://www.reddit.com/users/search.json?q=" + query)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String json = response.body().string();
                JSONObject data = new JSONObject(json).getJSONObject("data");
                JSONArray children = data.getJSONArray("children");
                List<Suggestion> users = new ArrayList<>();
                for (int i = 0; i < children.length(); i++) {
                    JSONObject user = children.getJSONObject(i).getJSONObject("data");
                    String iconUrl = user.optString("icon_img");
                    if (iconUrl.isEmpty()) {
                        iconUrl = user.optString("snoovatar_img");
                    }
                    users.add(new Suggestion(user.getString("name"), iconUrl));
                }
                return users;
            }
        } catch (IOException | JSONException e) {
            Log.e("FetchListOfUsersTask", "Error fetching users", e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<Suggestion> users) {
        if (users != null) {
            listener.onUsersFetched(users);
        }
    }
}
