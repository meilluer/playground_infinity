package ml.docilealligator.infinityforreddit.utils;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import ml.docilealligator.infinityforreddit.apis.GeminiAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GeminiHelper {
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/";
    private static Retrofit retrofit;
    private static final Map<String, String> summaryCache = new ConcurrentHashMap<>();

    public interface SummaryCallback {
        void onSuccess(String summary);
        void onFailure(String error);
    }

    public static String getSummary(String link) {
        return summaryCache.get(link);
    }

    public static void cacheSummary(String link, String summary) {
        summaryCache.put(link, summary);
    }

    private static GeminiAPI getApi() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return retrofit.create(GeminiAPI.class);
    }

    public static void summarizeLink(String apiKey, String link, SummaryCallback callback) {
        String prompt = "Summarize this link in a short paragraph: " + link;
        JSONObject json = new JSONObject();
        try {
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();
            part.put("text", prompt);
            parts.put(part);
            content.put("parts", parts);
            contents.put(content);
            json.put("contents", contents);
        } catch (JSONException e) {
            callback.onFailure(e.getMessage());
            return;
        }

        getApi().generateContent(apiKey, json.toString()).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject responseJson = new JSONObject(response.body());
                        JSONArray candidates = responseJson.getJSONArray("candidates");
                        if (candidates.length() > 0) {
                            JSONObject candidate = candidates.getJSONObject(0);
                            JSONObject content = candidate.getJSONObject("content");
                            JSONArray parts = content.getJSONArray("parts");
                            if (parts.length() > 0) {
                                String text = parts.getJSONObject(0).getString("text");
                                callback.onSuccess(text);
                                return;
                            }
                        }
                        callback.onFailure("No summary generated");
                    } catch (JSONException e) {
                        callback.onFailure("Failed to parse response: " + e.getMessage());
                    }
                } else {
                    callback.onFailure("API Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }
}
