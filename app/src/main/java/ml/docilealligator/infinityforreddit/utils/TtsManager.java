package ml.docilealligator.infinityforreddit.utils;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.ArrayList;

public class TtsManager {
    private final TextToSpeech mTextToSpeech;
    private boolean mIsInitialized = false;
    private final ArrayList<TtsChunk> mPendingChunks = new ArrayList<>();
    private Runnable mOnDone;
    private String mCurrentUtteranceId;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    private static class TtsChunk {
        String text;
        String utteranceId;
        
        TtsChunk(String text, String utteranceId) {
            this.text = text;
            this.utteranceId = utteranceId;
        }
    }

    public TtsManager(Context context) {
        mTextToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                mIsInitialized = true;
                if (!mPendingChunks.isEmpty()) {
                     processPendingChunks();
                }
            }
        });
        
        mTextToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
            }

            @Override
            public void onDone(String utteranceId) {
                if (utteranceId.equals(mCurrentUtteranceId)) {
                    if (mOnDone != null) {
                         mMainHandler.post(mOnDone);
                    }
                }
            }

            @Override
            public void onError(String utteranceId) {
            }
        });
    }

    public void speak(String text) {
        speak(text, null);
    }

    public void speak(String text, Runnable onDone) {
        stop();
        mOnDone = onDone;
        
        if (text == null || text.trim().isEmpty()) {
            if (onDone != null) onDone.run();
            return;
        }

        mPendingChunks.clear();

        // Robust Chunking Logic:
        // Split by length (3900 chars) to ensure we don't exceed TTS limits.
        // We do NOT split by newlines merely for the sake of splitting, 
        // because TTS engines handle newlines (pauses) correctly.
        // This avoids the issue where splitting by paragraph caused stops.
        
        int start = 0;
        int chunkCounter = 0;
        int len = text.length();
        
        while (start < len) {
            int end = Math.min(start + 3900, len);
            if (end < len) {
                // Try to find a safe split point (last space) to avoid cutting words
                int lastSpace = text.lastIndexOf(' ', end);
                if (lastSpace > start) {
                    end = lastSpace + 1;
                }
            }
            
            String subPart = text.substring(start, end);
            // Only add if it has content
            if (!subPart.trim().isEmpty()) {
                addChunk(subPart, chunkCounter++);
            }
            start = end;
        }

        if (mIsInitialized) {
            processPendingChunks();
        }
    }
    
    private void addChunk(String text, int idSuffix) {
        String utteranceId = System.currentTimeMillis() + "_" + idSuffix;
        TtsChunk chunk = new TtsChunk(text, utteranceId);
        mPendingChunks.add(chunk);
    }

    private void processPendingChunks() {
        if (mPendingChunks.isEmpty()) return;
        
        int i = 0;
        for (TtsChunk chunk : mPendingChunks) {
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, chunk.utteranceId);
            
            // Mark the last utterance ID to know when to trigger onDone
            if (i == mPendingChunks.size() - 1) {
                mCurrentUtteranceId = chunk.utteranceId;
            }
            
            // QUEUE_FLUSH for the first chunk to clear any old speech, QUEUE_ADD for the rest
            int queueMode = (i == 0) ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD;
            mTextToSpeech.speak(chunk.text, queueMode, params, chunk.utteranceId);
            i++;
        }
        mPendingChunks.clear();
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