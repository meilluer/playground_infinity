package ml.docilealligator.infinityforreddit.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

public class HeadphoneManager {

    private static HeadphoneManager instance;
    private Context context;
    private HeadphoneStateReceiver receiver;
    private boolean headphonesConnected = false;

    private HeadphoneManager(Context context) {
        this.context = context.getApplicationContext();
        receiver = new HeadphoneStateReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        this.context.registerReceiver(receiver, filter);

        // Initial check for headphone state
        AudioManager audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            headphonesConnected = audioManager.isWiredHeadsetOn() || audioManager.isBluetoothA2dpOn();
        }
    }

    public static synchronized HeadphoneManager getInstance(Context context) {
        if (instance == null) {
            instance = new HeadphoneManager(context);
        }
        return instance;
    }

    public boolean areHeadphonesConnected() {
        return headphonesConnected;
    }

    public void setHeadphonesConnected(boolean connected) {
        this.headphonesConnected = connected;
    }

    public void unregisterReceiver() {
        context.unregisterReceiver(receiver);
    }
}