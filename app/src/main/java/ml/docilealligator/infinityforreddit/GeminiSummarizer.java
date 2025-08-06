package ml.docilealligator.infinityforreddit;
import static ml.docilealligator.infinityforreddit.utils.APIUtils.gemini;

import okhttp3.*;
import com.google.gson.*;

public class GeminiSummarizer {
    private static final String API_KEY = gemini;//change with gemini
    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;
    public static String summaryResult = "";

    public static void summarizeWithGemini(String inputText) {
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

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                summaryResult = jsonResponse
                        .getAsJsonArray("candidates")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0).getAsJsonObject()
                        .get("text").getAsString();
            } else {
                summaryResult = "Error: " + response.message();
            }
        } catch (Exception e) {
            e.printStackTrace();
            summaryResult = "Exception: " + e.getMessage();
        }
    }
}




