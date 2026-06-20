package ml.docilealligator.infinityforreddit.archive;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ArcticShiftClient {
    private static final String BASE_URL = "https://arctic-shift.photon-reddit.com";
    private static final int TIMEOUT_MS = 5000;

    Map<String, ArcticShiftThing> getPostsByIds(List<String> ids) throws IOException, JSONException {
        return getThings("/api/posts/ids?fields=id,author,title,selftext&ids=", ids);
    }

    Map<String, ArcticShiftThing> getCommentsByIds(List<String> ids) throws IOException, JSONException {
        return getThings("/api/comments/ids?fields=id,author,body&ids=", ids);
    }

    private Map<String, ArcticShiftThing> getThings(String pathAndQueryPrefix, List<String> ids)
            throws IOException, JSONException {
        Map<String, ArcticShiftThing> things = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            return things;
        }

        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + pathAndQueryPrefix + joinIds(ids)).openConnection();
        connection.setConnectTimeout(TIMEOUT_MS);
        connection.setReadTimeout(TIMEOUT_MS);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        try {
            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                return things;
            }

            JSONObject response = new JSONObject(readBody(connection.getInputStream()));
            JSONArray data = response.optJSONArray("data");
            if (data == null) {
                return things;
            }

            for (int i = 0; i < data.length(); i++) {
                JSONObject thing = data.getJSONObject(i);
                String id = thing.optString("id", null);
                if (id == null) {
                    continue;
                }

                things.put(id, new ArcticShiftThing(
                        id,
                        thing.optString("author", null),
                        thing.optString("title", null),
                        thing.optString("selftext", null),
                        thing.optString("body", null)));
            }
            return things;
        } finally {
            connection.disconnect();
        }
    }

    private String readBody(InputStream inputStream) throws IOException {
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }
        return body.toString();
    }

    private String joinIds(List<String> ids) {
        StringBuilder builder = new StringBuilder();
        for (String id : ids) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(id);
        }
        return builder.toString();
    }
}
