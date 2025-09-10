import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.media.AudioManager;

public class HeadphoneDetector {
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public boolean areWiredHeadphonesConnected(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null||bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {

        }
        return false;
    }
}
