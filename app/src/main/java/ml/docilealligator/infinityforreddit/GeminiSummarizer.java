package ml.docilealligator.infinityforreddit;
import static ml.docilealligator.infinityforreddit.utils.APIUtils.gemini;

import okhttp3.*;
import com.google.gson.*;
import java.io.IOException;

public class GeminiSummarizer {
    private static final String API_KEY = gemini;
    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    public interface GeminiCallback {
        void onSuccess(String result);
        void onError(String error);
    }

    public static void summarizeWithGemini(String inputText, GeminiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        JsonObject part = new JsonObject();
        part.addProperty("text", "Summarize the following:\n" + inputText);
        JsonObject partsWrapper = new JsonObject();
        JsonArray partsArray = new JsonArray();
        partsArray.add(part);
        partsWrapper.add("parts", partsArray);

        JsonArray contents = new JsonArray();
        contents.add(partsWrapper);
        JsonObject payload = new JsonObject();
        payload.add("contents", contents);

        RequestBody body = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(ENDPOINT)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                        String result = jsonResponse
                                .getAsJsonArray("candidates")
                                .get(0).getAsJsonObject()
                                .getAsJsonObject("content")
                                .getAsJsonArray("parts")
                                .get(0).getAsJsonObject()
                                .get("text").getAsString();
                        callback.onSuccess(result);
                    } catch (Exception e) {
                        callback.onError("Parse error: " + e.getMessage());
                    }
                } else {
                    callback.onError("API error: " + response.message());
                }
            }
        });
    }
}




