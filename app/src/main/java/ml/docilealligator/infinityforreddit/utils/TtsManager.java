package ml.docilealligator.infinityforreddit.utils;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TtsManager {
    private final TextToSpeech mTextToSpeech;
    private boolean mIsInitialized = false;
    private final ArrayList<TtsChunk> mPendingChunks = new ArrayList<>();
    // Map to store chunks by utteranceId for retrieval in callbacks
    private final Map<String, TtsChunk> mChunkMap = new HashMap<>();
    private Runnable mOnDone;
    private OnTtsUpdateListener mOnTtsUpdateListener;
    private String mCurrentUtteranceId;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    public interface OnTtsUpdateListener {
        void onSentenceStart(String text, int start, int end);
        void onWordStart(String text, int start, int end);
    }

    private static class TtsChunk {
        String text;
        String utteranceId;
        int start;
        int end;
        
        TtsChunk(String text, String utteranceId, int start, int end) {
            this.text = text;
            this.utteranceId = utteranceId;
            this.start = start;
            this.end = end;
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
                if (mOnTtsUpdateListener != null) {
                    TtsChunk chunk = mChunkMap.get(utteranceId);
                    if (chunk != null) {
                        mMainHandler.post(() -> mOnTtsUpdateListener.onSentenceStart(chunk.text, chunk.start, chunk.end));
                    }
                }
            }

            @Override
            public void onRangeStart(String utteranceId, int start, int end, int frame) {
                if (mOnTtsUpdateListener != null) {
                    TtsChunk chunk = mChunkMap.get(utteranceId);
                    if (chunk != null) {
                        mMainHandler.post(() -> mOnTtsUpdateListener.onWordStart(chunk.text, start, end));
                    }
                }
            }

            @Override
            public void onDone(String utteranceId) {
                // Remove processed chunk to save memory, though for short texts it doesn't matter much
                mChunkMap.remove(utteranceId);
                
                if (utteranceId.equals(mCurrentUtteranceId)) {
                    if (mOnDone != null) {
                         mMainHandler.post(mOnDone);
                    }
                }
            }

            @Override
            public void onError(String utteranceId) {
                mChunkMap.remove(utteranceId);
            }
        });
    }

    public void speak(String text) {
        speak(text, null, null);
    }

    public void speak(String text, Runnable onDone) {
        speak(text, onDone, null);
    }

    public void speak(String text, Runnable onDone, OnTtsUpdateListener onTtsUpdateListener) {
        stop();
        mOnDone = onDone;
        mOnTtsUpdateListener = onTtsUpdateListener;
        
        if (text == null || text.trim().isEmpty()) {
            if (onDone != null) onDone.run();
            return;
        }

        mPendingChunks.clear();
        mChunkMap.clear();

        // Split by sentences using BreakIterator
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.getDefault());
        iterator.setText(text);
        int start = iterator.first();
        int chunkCounter = 0;
        
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String sentence = text.substring(start, end);
            if (!sentence.trim().isEmpty()) {
                // Check if sentence is too long for TTS (unlikely but possible)
                if (sentence.length() > 3900) {
                     // Fallback to splitting by length if a single sentence is huge
                     int subStart = 0;
                     int len = sentence.length();
                     while (subStart < len) {
                         int subEnd = Math.min(subStart + 3900, len);
                         addChunk(sentence.substring(subStart, subEnd), chunkCounter++, start + subStart, start + subEnd);
                         subStart = subEnd;
                     }
                } else {
                    addChunk(sentence, chunkCounter++, start, end);
                }
            }
        }

        if (mIsInitialized) {
            processPendingChunks();
        }
    }
    
    private void addChunk(String text, int idSuffix, int start, int end) {
        String utteranceId = System.currentTimeMillis() + "_" + idSuffix;
        TtsChunk chunk = new TtsChunk(text, utteranceId, start, end);
        mPendingChunks.add(chunk);
        mChunkMap.put(utteranceId, chunk);
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