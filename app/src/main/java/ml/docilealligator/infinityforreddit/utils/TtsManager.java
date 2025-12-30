package ml.docilealligator.infinityforreddit.utils;

import static ml.docilealligator.infinityforreddit.utils.APIUtils.Elevenlabs;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TtsManager {

    private TextView textView;
    private String displayedText;
    private final OkHttpClient client = new OkHttpClient();
    private MediaPlayer mediaPlayer;
    private final Context context;
    private File currentTempFile;
    private JSONObject alignment;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable highlightRunnable;
    private int currentHighlightedStart = -1;
    private int currentHighlightedEnd = -1;

    public TtsManager(Context context) {
        this.context = context;
    }

    public void speak(String text, TextView textView) {
        stop(); // Stop any existing playback and clear file

        this.textView = textView;
        this.displayedText = text;
        
        if (this.textView != null) {
            this.textView.setText(text);
        }

        String apiKey = Elevenlabs;
        String voiceId = "JBFqnCBsd6RMkjVDRZzb"; 
        String url = "https://api.elevenlabs.io/v1/text-to-speech/" + voiceId + "/with-timestamps";

        JSONObject json = new JSONObject();
        try {
            json.put("text", text);
            json.put("model_id", "eleven_multilingual_v2");
            JSONObject voiceSettings = new JSONObject();
            voiceSettings.put("stability", 0.5);
            voiceSettings.put("similarity_boost", 0.75);
            json.put("voice_settings", voiceSettings);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("xi-api-key", apiKey)
                .addHeader("Content-Type", "application/json")
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
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String audioBase64 = jsonResponse.getString("audio_base64");
                        alignment = jsonResponse.getJSONObject("alignment");

                        JSONArray charsArray = alignment.getJSONArray("characters");
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < charsArray.length(); i++) {
                            sb.append(charsArray.getString(i));
                        }
                        displayedText = sb.toString();

                        byte[] audioBytes = android.util.Base64.decode(audioBase64, android.util.Base64.DEFAULT);
                        File tempAudioFile = File.createTempFile("tts_audio", ".mp3", context.getCacheDir());
                        tempAudioFile.deleteOnExit();
                        currentTempFile = tempAudioFile;

                        try (FileOutputStream outputStream = new FileOutputStream(tempAudioFile)) {
                            outputStream.write(audioBytes);
                        }

                        handler.post(() -> {
                            try {
                                if (TtsManager.this.textView != null) {
                                    TtsManager.this.textView.setText(displayedText);
                                }
                                
                                mediaPlayer = new MediaPlayer();
                                mediaPlayer.setDataSource(tempAudioFile.getAbsolutePath());
                                mediaPlayer.prepare();
                                mediaPlayer.start();
                                startHighlighting();
                                mediaPlayer.setOnCompletionListener(mp -> {
                                    stop();
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                stop();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void startHighlighting() {
        highlightRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    boolean isPlaying = false;
                    try {
                        isPlaying = mediaPlayer.isPlaying();
                    } catch (IllegalStateException ignore) {}
                    
                    if (isPlaying) {
                        updateHighlight();
                        handler.postDelayed(this, 30); // Faster polling for better sync
                    }
                }
            }
        };
        handler.post(highlightRunnable);
    }

    private void updateHighlight() {
        if (mediaPlayer == null || textView == null || alignment == null || displayedText == null) return;

        try {
            // Add a small offset (e.g. 50ms) to account for processing lag
            double currentTime = (mediaPlayer.getCurrentPosition() + 50) / 1000.0;
            JSONArray startTimes = alignment.getJSONArray("character_start_times_seconds");
            JSONArray endTimes = alignment.getJSONArray("character_end_times_seconds");

            int charIndex = -1;
            for (int i = 0; i < startTimes.length(); i++) {
                if (currentTime >= startTimes.getDouble(i) && currentTime < endTimes.getDouble(i)) {
                    charIndex = i;
                    break;
                }
            }

            if (charIndex != -1 && charIndex < displayedText.length()) {
                int start = charIndex;
                while (start > 0 && !Character.isWhitespace(displayedText.charAt(start - 1))) {
                    start--;
                }
                int end = charIndex;
                while (end < displayedText.length() && !Character.isWhitespace(displayedText.charAt(end))) {
                    end++;
                }

                if (start != currentHighlightedStart || end != currentHighlightedEnd) {
                    currentHighlightedStart = start;
                    currentHighlightedEnd = end;
                    
                    Spannable spannable = new SpannableString(displayedText);
                    // Use Material Yellow 200 with higher alpha for better visibility
                    spannable.setSpan(new BackgroundColorSpan(Color.parseColor("#B3FFF59D")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    textView.setText(spannable, TextView.BufferType.SPANNABLE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (highlightRunnable != null) {
            handler.removeCallbacks(highlightRunnable);
        }
        currentHighlightedStart = -1;
        currentHighlightedEnd = -1;

        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            } catch (IllegalStateException e) {
                // ignore
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (currentTempFile != null && currentTempFile.exists()) {
            currentTempFile.delete();
            currentTempFile = null;
        }

        if (textView != null && displayedText != null) {
            textView.setText(displayedText);
        }
    }

    public void shutdown() {
        stop();
    }
}