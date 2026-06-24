package com.meilluer.infinity.customviews;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;

import com.meilluer.infinity.utils.SharedPreferencesUtils;
import com.meilluer.infinity.videoautoplay.Config;
import com.meilluer.infinity.videoautoplay.DefaultExoCreator;
import com.meilluer.infinity.videoautoplay.ToroExo;

@UnstableApi
public class LoopAvailableExoCreator extends DefaultExoCreator {
    private final SharedPreferences sharedPreferences;

    public LoopAvailableExoCreator(@NonNull ToroExo toro, @NonNull Config config, SharedPreferences sharedPreferences) {
        super(toro, config);
        this.sharedPreferences = sharedPreferences;
    }

    @NonNull
    @Override
    public ExoPlayer createPlayer() {
        ExoPlayer player = super.createPlayer();
        if (sharedPreferences.getBoolean(SharedPreferencesUtils.LOOP_VIDEO, true)) {
            player.setRepeatMode(Player.REPEAT_MODE_ALL);
        } else {
            player.setRepeatMode(Player.REPEAT_MODE_OFF);
        }

        return player;
    }
}
