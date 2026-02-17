package ml.docilealligator.infinityforreddit.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.preference.Preference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customviews.preference.CustomFontPreferenceFragmentCompat;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProxyPreferenceFragment extends CustomFontPreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Preference actualIpPref;
    private Preference proxyIpPref;
    private Preference actualLocationPref;
    private Preference proxyLocationPref;
    private Preference redditStatusPref;

    private OkHttpClient directClient;
    private OkHttpClient proxiedClient;

    private Handler mainHandler;

    private SharedPreferences mProxySharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.proxy_preferences, rootKey);

        actualIpPref = findPreference("proxy_actual_ip");
        proxyIpPref = findPreference("proxy_proxy_ip");
        actualLocationPref = findPreference("proxy_actual_location");
        proxyLocationPref = findPreference("proxy_proxy_location");
        redditStatusPref = findPreference("proxy_reddit_connection_status");

        mainHandler = new Handler(Looper.getMainLooper());

        mProxySharedPreferences = getContext().getSharedPreferences(SharedPreferencesUtils.PROXY_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        mProxySharedPreferences.registerOnSharedPreferenceChangeListener(this);

        setupClients();
        fetchStatus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mProxySharedPreferences != null) {
            mProxySharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (SharedPreferencesUtils.PROXY_ENABLED.equals(key) ||
                SharedPreferencesUtils.PROXY_TYPE.equals(key) ||
                SharedPreferencesUtils.PROXY_HOSTNAME.equals(key) ||
                SharedPreferencesUtils.PROXY_PORT.equals(key)) {
            setupClients();
            fetchStatus();
        }
    }

    private void setupClients() {
        directClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .proxy(Proxy.NO_PROXY)
                .build();

        boolean proxyEnabled = mProxySharedPreferences.getBoolean(SharedPreferencesUtils.PROXY_ENABLED, false);

        OkHttpClient.Builder proxiedBuilder = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS);

        if (proxyEnabled) {
            String typeStr = mProxySharedPreferences.getString(SharedPreferencesUtils.PROXY_TYPE, "HTTP");
            try {
                Proxy.Type proxyType = Proxy.Type.valueOf(typeStr);
                if (proxyType != Proxy.Type.DIRECT) {
                    String proxyHost = mProxySharedPreferences.getString(SharedPreferencesUtils.PROXY_HOSTNAME, "127.0.0.1");
                    int proxyPort = Integer.parseInt(mProxySharedPreferences.getString(SharedPreferencesUtils.PROXY_PORT, "1080"));

                    InetSocketAddress proxyAddr = new InetSocketAddress(proxyHost, proxyPort);
                    Proxy proxy = new Proxy(proxyType, proxyAddr);
                    proxiedBuilder.proxy(proxy);
                }
            } catch (Exception e) {
                // Ignore or log
            }
        }

        proxiedClient = proxiedBuilder.build();
    }

    private void fetchStatus() {
        Context context = getContext();
        if (context == null) return;

        updatePref(actualIpPref, getString(R.string.settings_proxy_status_fetching));
        updatePref(proxyIpPref, getString(R.string.settings_proxy_status_fetching));
        updatePref(actualLocationPref, getString(R.string.settings_proxy_status_fetching));
        updatePref(proxyLocationPref, getString(R.string.settings_proxy_status_fetching));
        updatePref(redditStatusPref, getString(R.string.settings_proxy_status_fetching));

        Request ipRequest = new Request.Builder()
                .url("http://ip-api.com/json")
                .header("User-Agent", APIUtils.USER_AGENT)
                .build();

        // Fetch Actual IP
        directClient.newCall(ipRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                updatePref(actualIpPref, getString(R.string.settings_proxy_status_error));
                updatePref(actualLocationPref, getString(R.string.settings_proxy_status_error));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (Response res = response) {
                    if (res.isSuccessful() && res.body() != null) {
                        try {
                            String body = res.body().string();
                            JSONObject json = new JSONObject(body);
                            String ip = json.optString("query", "N/A");
                            String city = json.optString("city", "");
                            String country = json.optString("country", "");
                            String location = (city.isEmpty() || country.isEmpty()) ? "N/A" : String.format("%s, %s", city, country);
                            updatePref(actualIpPref, ip);
                            updatePref(actualLocationPref, location);
                        } catch (JSONException | IOException e) {
                            updatePref(actualIpPref, getString(R.string.settings_proxy_status_error));
                            updatePref(actualLocationPref, getString(R.string.settings_proxy_status_error));
                        }
                    } else {
                        updatePref(actualIpPref, getString(R.string.settings_proxy_status_error));
                        updatePref(actualLocationPref, getString(R.string.settings_proxy_status_error));
                    }
                }
            }
        });

        boolean proxyEnabled = mProxySharedPreferences.getBoolean(SharedPreferencesUtils.PROXY_ENABLED, false);
        String proxyTypeStr = mProxySharedPreferences.getString(SharedPreferencesUtils.PROXY_TYPE, "HTTP");
        boolean isDirect = "DIRECT".equals(proxyTypeStr);

        if (!proxyEnabled || isDirect) {
            updatePref(proxyIpPref, "N/A");
            updatePref(proxyLocationPref, "N/A");
            checkRedditStatus(proxyEnabled && !isDirect);
        } else {
            // Fetch Proxy IP
            proxiedClient.newCall(ipRequest).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    updatePref(proxyIpPref, getString(R.string.settings_proxy_status_error));
                    updatePref(proxyLocationPref, getString(R.string.settings_proxy_status_error));
                    checkRedditStatus(true);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try (Response res = response) {
                        if (res.isSuccessful() && res.body() != null) {
                            try {
                                String body = res.body().string();
                                JSONObject json = new JSONObject(body);
                                String ip = json.optString("query", "N/A");
                                String city = json.optString("city", "");
                                String country = json.optString("country", "");
                                String location = (city.isEmpty() || country.isEmpty()) ? "N/A" : String.format("%s, %s", city, country);
                                updatePref(proxyIpPref, ip);
                                updatePref(proxyLocationPref, location);
                            } catch (JSONException | IOException e) {
                                updatePref(proxyIpPref, getString(R.string.settings_proxy_status_error));
                                updatePref(proxyLocationPref, getString(R.string.settings_proxy_status_error));
                            }
                        } else {
                            updatePref(proxyIpPref, getString(R.string.settings_proxy_status_error));
                            updatePref(proxyLocationPref, getString(R.string.settings_proxy_status_error));
                        }
                        checkRedditStatus(true);
                    }
                }
            });
        }
    }

    private void checkRedditStatus(boolean useProxy) {
        // Check Reddit Connection
        Request redditRequest = new Request.Builder()
                .url("https://www.reddit.com/api/v1/scopes")
                .header("User-Agent", APIUtils.USER_AGENT)
                .build();

        proxiedClient.newCall(redditRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                updatePref(redditStatusPref, getString(R.string.settings_proxy_status_error));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (Response res = response) {
                    if (res.isSuccessful()) {
                        if (useProxy) {
                            updatePref(redditStatusPref, getString(R.string.settings_proxy_status_connected));
                        } else {
                            updatePref(redditStatusPref, getString(R.string.settings_proxy_status_direct));
                        }
                    } else {
                        updatePref(redditStatusPref, getString(R.string.settings_proxy_status_error) + " (" + res.code() + ")");
                    }
                }
            }
        });
    }

    private void updatePref(Preference pref, String summary) {
        mainHandler.post(() -> {
            if (pref != null) {
                pref.setSummary(summary);
            }
        });
    }
}