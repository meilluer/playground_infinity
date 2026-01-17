package ml.docilealligator.infinityforreddit.apis;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GeminiAPI {
    @Headers("Content-Type: application/json")
    @POST("v1beta/models/gemini-3-flash-preview:generateContent")
    Call<String> generateContent(@Query("key") String apiKey, @Body String body);
}
