package ml.docilealligator.infinityforreddit.utils;

import static ml.docilealligator.infinityforreddit.utils.APIUtils.Elevenlabs;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TtsManager {

    private TextView textView;
    private String text;
    private Spannable spannable;
    private final OkHttpClient client = new OkHttpClient();
    private MediaPlayer mediaPlayer;
    private final Context context;
    private File currentTempFile;

    public TtsManager(Context context) {
        this.context = context;
    }

    public void speak(String text, TextView textView) {
        stop(); // Stop any existing playback and clear file

        this.text = text;
        this.textView = textView;
        this.spannable = new SpannableString(text);
        if (this.textView != null) {
            this.textView.setText(spannable);
        }

        String apiKey = Elevenlabs;
        String voiceId = "JBFqnCBsd6RMkjVDRZzb"; // Example voice ID from the provided curl format
        String url = "https://api.elevenlabs.io/v1/text-to-speech/" + voiceId + "?output_format=mp3_44100_128";

        JSONObject json = new JSONObject();
        try {
            json.put("text", text);
            json.put("model_id", "eleven_multilingual_v2");
            JSONObject voiceSettings = new JSONObject();
            voiceSettings.put("stability", 0);
            voiceSettings.put("similarity_boost", 0);
            json.put("voice_settings", voiceSettings);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("xi-api-key", apiKey)
                .addHeader("Content-Type", "application/json") // Add Content-Type header
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try (InputStream inputStream = response.body().byteStream()) {
                        File tempAudioFile = File.createTempFile("tts_audio", ".mp3", context.getCacheDir());
                        tempAudioFile.deleteOnExit();
                        currentTempFile = tempAudioFile;

                        try (FileOutputStream outputStream = new FileOutputStream(tempAudioFile)) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                        }

                        try {
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setDataSource(tempAudioFile.getAbsolutePath());
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            mediaPlayer.setOnCompletionListener(mp -> {
                                stop();
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (tempAudioFile.exists()) {
                                tempAudioFile.delete();
                            }
                        }
                    }
                }
            }
        });
    }

    public void stop() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (currentTempFile != null && currentTempFile.exists()) {
            currentTempFile.delete();
            currentTempFile = null;
        }

        if (textView != null && text != null) {
            spannable = new SpannableString(text);
            textView.setText(spannable);
        }
    }

    public void shutdown() {
        stop();
    }
}
