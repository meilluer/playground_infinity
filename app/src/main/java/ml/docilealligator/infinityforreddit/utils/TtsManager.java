package ml.docilealligator.infinityforreddit.utils;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TtsManager {

    private static TextToSpeech tts;
    private static boolean isInitialized = false;
    private static WeakReference<TtsManager> currentInstanceRef;
    private static final String UTTERANCE_ID = "infinity_tts_id";

    private final Context context;
    private TextView textView;
    private String textToSpeak;
    private final Map<String, Integer> chunkOffsets = new HashMap<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable onComplete;
    private String lastUtteranceId;

    public TtsManager(Context context) {
        this.context = context;
        initializeTTS();
    }

    private void initializeTTS() {
        if (tts == null) {
            tts = new TextToSpeech(context.getApplicationContext(), status -> {
                if (status == TextToSpeech.SUCCESS) {
                    AudioAttributes audioAttributes = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build();
                    tts.setAudioAttributes(audioAttributes);
                    
                    int result = tts.setLanguage(Locale.getDefault());
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                         // Fallback to English if default is not available
                         tts.setLanguage(Locale.US);
                    }
                    
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            // Optional: Reset highlighting or UI state
                        }

                        @Override
                        public void onDone(String utteranceId) {
                           TtsManager manager = currentInstanceRef != null ? currentInstanceRef.get() : null;
                           if (manager != null && manager.onComplete != null && utteranceId.equals(manager.lastUtteranceId)) {
                               manager.handler.post(manager.onComplete);
                           }
                        }

                        @Override
                        public void onError(String utteranceId) {
                             // Handle error
                        }

                        @Override
                        public void onRangeStart(String utteranceId, int start, int end, int frame) {
                            TtsManager manager = currentInstanceRef != null ? currentInstanceRef.get() : null;
                            if (manager != null) {
                                manager.handleRangeStart(utteranceId, start, end);
                            }
                        }
                    });
                    isInitialized = true;
                    
                    // Attempt to speak if a request was made during initialization
                    TtsManager manager = currentInstanceRef != null ? currentInstanceRef.get() : null;
                    if (manager != null) {
                        manager.speakInternal();
                    }
                }
            });
        }
    }

    public void speak(String text, TextView textView) {
        speak(text, textView, null);
    }

    public void speak(String text, TextView textView, Runnable onComplete) {
        // Stop any previous playback globally to avoid overlap
        if (tts != null) {
            tts.stop();
        }
        
        currentInstanceRef = new WeakReference<>(this);
        this.textView = textView;
        this.onComplete = onComplete;
        
        if (this.textView != null) {
            // Use the text from TextView to ensure indices match for highlighting
            // and avoid speaking raw markdown if rendered text is available.
            this.textToSpeak = this.textView.getText().toString();
            // Reset old highlights instead of resetting the whole text
            clearHighlights();
        } else {
            this.textToSpeak = text;
        }

        if (isInitialized) {
            speakInternal();
        } else {
             Toast.makeText(context, "Initializing TTS engine...", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearHighlights() {
        if (textView == null) return;
        handler.post(() -> {
            try {
                CharSequence text = textView.getText();
                if (text instanceof Spannable) {
                    Spannable spannable = (Spannable) text;
                    BackgroundColorSpan[] spans = spannable.getSpans(0, spannable.length(), BackgroundColorSpan.class);
                    for (BackgroundColorSpan span : spans) {
                        spannable.removeSpan(span);
                    }
                }
            } catch (Exception e) {
                // Ignore
            }
        });
    }

    private void speakInternal() {
        if (tts != null && textToSpeak != null && !textToSpeak.trim().isEmpty()) {
            chunkOffsets.clear();
            int maxLength = TextToSpeech.getMaxSpeechInputLength();
            
            Bundle params = new Bundle();
            params.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC);
            params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f);
            
            if (textToSpeak.length() <= maxLength) {
                 lastUtteranceId = UTTERANCE_ID;
                 tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params, UTTERANCE_ID);
            } else {
                 // Chunking logic for long text
                 tts.speak("", TextToSpeech.QUEUE_FLUSH, null, null); // Clear queue
                 int offset = 0;
                 int chunkIndex = 0;
                 while (offset < textToSpeak.length()) {
                     int end = Math.min(offset + maxLength, textToSpeak.length());
                     // Try to split at whitespace to avoid cutting words
                     if (end < textToSpeak.length()) {
                         int lastSpace = textToSpeak.lastIndexOf(' ', end);
                         if (lastSpace > offset) {
                             end = lastSpace + 1; // Include space
                         }
                     }
                     String chunk = textToSpeak.substring(offset, end);
                     String chunkId = UTTERANCE_ID + "_" + chunkIndex;
                     chunkOffsets.put(chunkId, offset);
                     
                     tts.speak(chunk, TextToSpeech.QUEUE_ADD, params, chunkId);
                     lastUtteranceId = chunkId; // Update last ID
                     
                     offset = end;
                     chunkIndex++;
                 }
            }
        }
    }

    private void handleRangeStart(String utteranceId, int start, int end) {
        int globalOffset = 0;
        if (utteranceId.startsWith(UTTERANCE_ID + "_")) {
            Integer offset = chunkOffsets.get(utteranceId);
            if (offset != null) globalOffset = offset;
        }
        highlightText(globalOffset + start, globalOffset + end);
    }

    private void highlightText(int start, int end) {
        if (textView == null || textToSpeak == null) return;
        
        handler.post(() -> {
            try {
                 CharSequence currentText = textView.getText();
                 Spannable spannable;
                 if (currentText instanceof Spannable) {
                     spannable = (Spannable) currentText;
                 } else {
                     // Create a new SpannableString from current text to preserve existing spans (e.g. Markdown formatting)
                     spannable = new SpannableString(currentText);
                 }

                 // Remove old spans to keep only current word highlighted
                 BackgroundColorSpan[] spans = spannable.getSpans(0, spannable.length(), BackgroundColorSpan.class);
                 for (BackgroundColorSpan span : spans) {
                     spannable.removeSpan(span);
                 }
                 
                 // Apply new highlight if within bounds
                 if (start >= 0 && end <= spannable.length() && start < end) {
                     spannable.setSpan(new BackgroundColorSpan(Color.parseColor("#B3FFF59D")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                 }
                 
                 // Always update the text to ensure the UI reflects the change, 
                 // especially if we converted from Spanned to Spannable.
                 textView.setText(spannable);
            } catch (Exception e) {
                // Ignore errors to prevent crashes during playback
            }
        });
    }

    public void stop() {
        onComplete = null;
        if (tts != null) {
            tts.stop();
        }
        clearHighlights();
    }
    
    public void shutdown() {
        stop();
        // We do not shutdown the static TTS engine to reuse it.
    }
}
