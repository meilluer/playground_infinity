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

public class FetchListOfSubredditsTask extends AsyncTask<String, Void, List<Suggestion>> {
    private final Activity activity;
    private final OnSubredditsFetchedListener listener;

    public interface OnSubredditsFetchedListener {
        void onSubredditsFetched(List<Suggestion> subreddits);
    }

    public FetchListOfSubredditsTask(Activity activity, OnSubredditsFetchedListener listener) {
        this.activity = activity;
        this.listener = listener;
    }

    @Override
    protected List<Suggestion> doInBackground(String... params) {
        String query = params[0];
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://www.reddit.com/subreddits/search.json?q=" + query)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String json = response.body().string();
                JSONObject data = new JSONObject(json).getJSONObject("data");
                JSONArray children = data.getJSONArray("children");
                List<Suggestion> subreddits = new ArrayList<>();
                for (int i = 0; i < children.length(); i++) {
                    JSONObject subreddit = children.getJSONObject(i).getJSONObject("data");
                    String iconUrl = subreddit.optString("community_icon");
                    if (iconUrl.isEmpty()) {
                        iconUrl = subreddit.optString("icon_img");
                    }
                    subreddits.add(new Suggestion(subreddit.getString("display_name"), iconUrl));
                }
                return subreddits;
            }
        } catch (IOException | JSONException e) {
            Log.e("FetchListOfSubreddits", "Error fetching subreddits", e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<Suggestion> subreddits) {
        if (subreddits != null) {
            listener.onSubredditsFetched(subreddits);
        }
    }
}
