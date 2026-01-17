package ml.docilealligator.infinityforreddit;

import okhttp3.*;
import com.google.gson.*;
import java.io.IOException;

public class GeminiSummarizer {

    public interface GeminiCallback {
        void onSuccess(String result);
        void onError(String error);
    }

    public static void summarizeWithGemini(String apiKey, String inputText, GeminiCallback callback) {
        if (apiKey == null || apiKey.isEmpty()) {
            callback.onError("API Key is missing");
            return;
        }

        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=" + apiKey;

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
                .url(endpoint)
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
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    callback.onError("API error: " + response.message() + "\nDetails: " + errorBody);
                }
            }
        });
    }

    public static void translateWithGemini(String apiKey, String inputText, GeminiCallback callback) {
        if (apiKey == null || apiKey.isEmpty()) {
            callback.onError("API Key is missing");
            return;
        }

        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=" + apiKey;

        OkHttpClient client = new OkHttpClient();

        JsonObject part = new JsonObject();
        part.addProperty("text", "DO NOT RESPONSE WITH ANYTHING ELSE Translate the following text to english:\n" + inputText);
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
                .url(endpoint)
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
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    callback.onError("API error: " + response.message() + "\nDetails: " + errorBody);
                }
            }
        });
    }

    public static void translateTitleAndBodyWithGemini(String apiKey, String title, String body, GeminiCallback callback) {
        if (apiKey == null || apiKey.isEmpty()) {
            callback.onError("API Key is missing");
            return;
        }

        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=" + apiKey;

        OkHttpClient client = new OkHttpClient();

        JsonObject part = new JsonObject();
        String prompt = "Translate the following Title and Body to English. Format the output exactly as:\nTITLE: [Translated Title]\nBODY: [Translated Body]\n\nOriginal Content:\nTitle: " + title + "\nBody: " + (body == null ? "" : body);
        part.addProperty("text", prompt);
        JsonObject partsWrapper = new JsonObject();
        JsonArray partsArray = new JsonArray();
        partsArray.add(part);
        partsWrapper.add("parts", partsArray);

        JsonArray contents = new JsonArray();
        contents.add(partsWrapper);
        JsonObject payload = new JsonObject();
        payload.add("contents", contents);

        RequestBody bodyRequest = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(endpoint)
                .post(bodyRequest)
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
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    callback.onError("API error: " + response.message() + "\nDetails: " + errorBody);
                }
            }
        });
    }
}




