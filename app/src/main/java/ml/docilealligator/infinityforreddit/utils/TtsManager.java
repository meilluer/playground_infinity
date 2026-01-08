package ml.docilealligator.infinityforreddit.utils;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class TtsManager {
    private final TextToSpeech mTextToSpeech;
    private boolean mIsInitialized = false;
    private final ArrayList<String> mPendingText = new ArrayList<>();
    private Runnable mOnDone;
    private String mCurrentUtteranceId;

    public TtsManager(Context context) {
        mTextToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                mIsInitialized = true;
                if (!mPendingText.isEmpty()) {
                     processPendingText();
                }
            }
        });
        
        mTextToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
            }

            @Override
            public void onDone(String utteranceId) {
                if (mOnDone != null && utteranceId.equals(mCurrentUtteranceId)) {
                     mOnDone.run();
                }
            }

            @Override
            public void onError(String utteranceId) {
            }
        });
    }

    public void speak(String text, TextView textView) {
        speak(text, textView, null);
    }
    
    public void speak(String text, TextView textView, Runnable onDone) {
        stop();
        mOnDone = onDone;
        if (text == null || text.trim().isEmpty()) {
            if (onDone != null) onDone.run();
            return;
        }

        // Split text by newlines to handle paragraphs.
        // We also handle very long text chunks.
        String[] parts = text.split("\n+");
        mPendingText.clear();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                if (trimmed.length() > 3900) {
                     int chunk = 3900;
                     for (int i = 0; i < trimmed.length(); i += chunk) {
                         mPendingText.add(trimmed.substring(i, Math.min(trimmed.length(), i + chunk)));
                     }
                } else {
                    mPendingText.add(trimmed);
                }
            }
        }
        
        if (mIsInitialized) {
            processPendingText();
        }
    }

    private void processPendingText() {
        if (mPendingText.isEmpty()) {
            return;
        }
        
        int i = 0;
        for (String text : mPendingText) {
            HashMap<String, String> params = new HashMap<>();
            String utteranceId = String.valueOf(System.currentTimeMillis()) + "_" + i;
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
            
            // If it's the last one, we track it for onDone
            if (i == mPendingText.size() - 1) {
                mCurrentUtteranceId = utteranceId;
            }
            
            // First chunk flushes (clears previous), others add
            int queueMode = (i == 0) ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD;
            mTextToSpeech.speak(text, queueMode, params);
            i++;
        }
        mPendingText.clear();
    }
    
    public void stop() {
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
        }
    }

    public void shutdown() {
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }
    }
    
    public boolean isSpeaking() {
        return mTextToSpeech != null && mTextToSpeech.isSpeaking();
    }
}
