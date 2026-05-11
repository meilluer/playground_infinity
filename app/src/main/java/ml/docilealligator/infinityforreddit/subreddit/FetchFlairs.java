package ml.docilealligator.infinityforreddit.subreddit;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchFlairs {
    public static void fetchFlairsInSubreddit(Executor executor, Handler handler, Retrofit oauthRetrofit,
                                              String accessToken, String subredditName,
                                              FetchFlairsInSubredditListener fetchFlairsInSubredditListener) {
        oauthRetrofit.create(RedditAPI.class).getFlairs(APIUtils.getOAuthHeader(accessToken), subredditName)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            executor.execute(() -> {
                                List<Flair> flairs = null;
                                if (response.body() != null) {
                                    flairs = parseFlairs(response.body());
                                }
                                
                                final List<Flair> finalFlairs = flairs;
                                if (finalFlairs != null) {
                                    handler.post(() -> fetchFlairsInSubredditListener.fetchSuccessful(finalFlairs));
                                } else {
                                    handler.post(fetchFlairsInSubredditListener::fetchFailed);
                                }
                            });
                        } else if (response.code() == 403) {
                            //No flairs or access denied
                            fetchFlairsInSubredditListener.fetchSuccessful(null);
                        } else {
                            fetchFlairsInSubredditListener.fetchFailed();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                        fetchFlairsInSubredditListener.fetchFailed();
                    }
                });
    }

    @WorkerThread
    @Nullable
    private static List<Flair> parseFlairs(String response) {
        if (response == null) return null;
        try {
            JSONArray jsonArray = new JSONArray(response);
            List<Flair> flairs = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String id = jsonObject.optString(JSONUtils.ID_KEY);
                    String text = jsonObject.optString(JSONUtils.TEXT_KEY);
                    
                    // If text is missing, check "text" key again or "flair_text" just in case
                    if (text.isEmpty()) {
                        text = jsonObject.optString("flair_text");
                    }

                    if (!id.isEmpty() && !text.isEmpty()) {
                        boolean editable = jsonObject.optBoolean(JSONUtils.TEXT_EDITABLE_KEY, false);
                        flairs.add(new Flair(id, text, editable));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return flairs;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public interface FetchFlairsInSubredditListener {
        void fetchSuccessful(List<Flair> flairs);

        void fetchFailed();
    }
}
