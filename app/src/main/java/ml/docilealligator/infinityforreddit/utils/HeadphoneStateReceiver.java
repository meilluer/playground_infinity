package ml.docilealligator.infinityforreddit.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class HeadphoneStateReceiver extends BroadcastReceiver {

    private HeadphoneManager headphoneManager;

    public HeadphoneStateReceiver(HeadphoneManager headphoneManager) {
        this.headphoneManager = headphoneManager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())) {
            int state = intent.getIntExtra("state", -1);
            if (state == 0) {
                // Headset unplugged
                headphoneManager.setHeadphonesConnected(false);
            } else if (state == 1) {
                // Headset plugged
                headphoneManager.setHeadphonesConnected(true);
            }
        } else if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
            // Headphones disconnected (e.g., Bluetooth headphones)
            headphoneManager.setHeadphonesConnected(false);
        }
    }
}