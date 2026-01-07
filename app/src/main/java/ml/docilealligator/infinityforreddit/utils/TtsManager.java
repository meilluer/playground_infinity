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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TtsManager {

    private static TextToSpeech tts;
    private static boolean isInitialized = false;
    private static TtsManager currentInstance;
    private static final String UTTERANCE_ID = "infinity_tts_id";

    private final Context context;
    private TextView textView;
    private String textToSpeak;
    private final Map<String, Integer> chunkOffsets = new HashMap<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable onComplete;
    
    // Track total chunks to know when to finish
    private int totalChunks = 0;

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
                            TtsManager manager = currentInstance;
                            if (manager != null) {
                                manager.handleDone(utteranceId);
                            }
                        }

                        @Override
                        public void onError(String utteranceId) {
                             // Proceed to next even if error? 
                             // With QUEUE_ADD, the system handles the queue. 
                             // We just need to check if we are done.
                             TtsManager manager = currentInstance;
                             if (manager != null) {
                                 manager.handleDone(utteranceId);
                             }
                        }

                        @Override
                        public void onRangeStart(String utteranceId, int start, int end, int frame) {
                            TtsManager manager = currentInstance;
                            if (manager != null) {
                                manager.handleRangeStart(utteranceId, start, end);
                            }
                        }
                    });
                    isInitialized = true;
                    
                    // Attempt to speak if a request was made during initialization
                    TtsManager manager = currentInstance;
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
        
        currentInstance = this;
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
            synchronized (chunkOffsets) {
                chunkOffsets.clear();
            }
            
            int maxLength = TextToSpeech.getMaxSpeechInputLength();
            int offset = 0;
            int chunkIndex = 0;
            
            // Generate all chunks first
            List<String> chunks = new ArrayList<>();
            List<Integer> offsets = new ArrayList<>();

            while (offset < textToSpeak.length()) {
                int end = Math.min(offset + maxLength, textToSpeak.length());
                
                // Prioritize splitting at newlines to handle paragraphs correctly
                int firstNewline = textToSpeak.indexOf('\n', offset);
                if (firstNewline != -1 && firstNewline < end) {
                    end = firstNewline + 1; // Include the newline
                } else {
                    // If no newline within range, try to split at whitespace
                    if (end < textToSpeak.length()) {
                        int lastSpace = textToSpeak.lastIndexOf(' ', end);
                        if (lastSpace > offset) {
                            end = lastSpace + 1;
                        }
                    }
                }

                String chunk = textToSpeak.substring(offset, end);
                
                // Skip empty chunks but ensure we advance offset
                // Note: keeping whitespace only chunks can help with pacing if needed, 
                // but usually better to skip.
                if (!chunk.trim().isEmpty()) {
                    chunks.add(chunk);
                    offsets.add(offset);
                    
                    String chunkId = UTTERANCE_ID + "_" + chunkIndex;
                    synchronized (chunkOffsets) {
                        chunkOffsets.put(chunkId, offset);
                    }
                    chunkIndex++;
                }

                offset = end;
            }
            
            totalChunks = chunks.size();
            
            if (totalChunks == 0) {
                 if (onComplete != null) onComplete.run();
                 if (currentInstance == this) currentInstance = null;
                 return;
            }

            // Queue all chunks
            for (int i = 0; i < totalChunks; i++) {
                String chunk = chunks.get(i);
                String chunkId = UTTERANCE_ID + "_" + i;
                
                Bundle params = new Bundle();
                params.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC);
                params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f);
                
                // First chunk flushes (clears) previous queue, others add to it
                int queueMode = (i == 0) ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD;
                
                tts.speak(chunk, queueMode, params, chunkId);
            }
        }
    }
    
    private void handleDone(String utteranceId) {
        // Check if this is the last chunk
        if (utteranceId != null && utteranceId.startsWith(UTTERANCE_ID + "_")) {
            try {
                int index = Integer.parseInt(utteranceId.substring(UTTERANCE_ID.length() + 1));
                if (index == totalChunks - 1) {
                    // This was the last chunk
                    handler.post(() -> {
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    });
                    
                    // Release strong reference
                    if (currentInstance == this) {
                        currentInstance = null;
                    }
                }
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
    }

    private void handleRangeStart(String utteranceId, int start, int end) {
        int globalOffset = 0;
        if (utteranceId.startsWith(UTTERANCE_ID + "_")) {
            Integer offset;
            synchronized (chunkOffsets) {
                offset = chunkOffsets.get(utteranceId);
            }
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
        
        if (currentInstance == this) {
            currentInstance = null;
        }
    }
    
    public void shutdown() {
        stop();
        // We do not shutdown the static TTS engine to reuse it.
    }
}